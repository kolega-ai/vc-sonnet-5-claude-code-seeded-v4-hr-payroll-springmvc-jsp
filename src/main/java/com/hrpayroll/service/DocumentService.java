package com.hrpayroll.service;

import com.hrpayroll.domain.DocumentCategory;
import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.HrDocument;
import com.hrpayroll.repository.HrDocumentRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentService {

    private final HrDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public DocumentService(HrDocumentRepository documentRepository, FileStorageService fileStorageService,
                            AuditService auditService, NotificationService notificationService) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public List<HrDocument> forEmployee(Long employeeId) {
        return documentRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
    }

    public List<HrDocument> all() {
        return documentRepository.findAllByOrderByUploadedAtDesc();
    }

    public HrDocument getOrThrow(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    @Transactional
    public HrDocument upload(Employee owner, Employee uploader, DocumentCategory category, MultipartFile file) {
        String stored = fileStorageService.store(file);
        HrDocument doc = new HrDocument();
        doc.setEmployee(owner);
        doc.setCategory(category);
        doc.setDisplayName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        doc.setStoredFilename(stored);
        doc.setContentType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setUploadedBy(uploader);
        HrDocument saved = documentRepository.save(doc);
        notificationService.notify(owner, "A new document was uploaded to your profile: " + saved.getDisplayName(),
                "/employee/documents");
        return saved;
    }

    /** Registers a file an employee attached to an HR request against their record. */
    @Transactional
    public HrDocument attachToCase(Employee owner, Employee uploader, MultipartFile file) {
        String stored = fileStorageService.storeCaseAttachment(file);
        HrDocument doc = new HrDocument();
        doc.setEmployee(owner);
        doc.setCategory(DocumentCategory.REQUEST_ATTACHMENT);
        doc.setDisplayName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment");
        doc.setStoredFilename(stored);
        doc.setContentType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setUploadedBy(uploader);
        return documentRepository.save(doc);
    }

    public org.springframework.core.io.Resource getResource(HrDocument doc) {
        return fileStorageService.loadAsResource(doc.getStoredFilename());
    }

    /** Reads a stored document back as UTF-8 text for the review assistants. */
    public String readText(HrDocument doc) {
        try (java.io.InputStream in = getResource(doc).getInputStream()) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read " + doc.getDisplayName(), e);
        }
    }

    public java.util.List<String> listBatch(String batchPath) {
        return fileStorageService.listBatchDir(batchPath);
    }

    /**
     * Reads the totals block out of a payslip the payroll bureau delivered as
     * XML, so the payroll desk can check it against the portal figures without
     * opening the file by hand.
     */
    public java.util.Map<String, String> readDocTotals(HrDocument doc) {
        try (java.io.InputStream in = getResource(doc).getInputStream()) {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document parsed = dbf.newDocumentBuilder().parse(in);
            java.util.Map<String, String> totals = new java.util.LinkedHashMap<>();
            org.w3c.dom.NodeList nodes = parsed.getElementsByTagName("total");
            for (int i = 0; i < nodes.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) nodes.item(i);
                totals.put(el.getAttribute("name"), el.getTextContent());
            }
            return totals;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read totals from " + doc.getDisplayName(), e);
        }
    }

    /**
     * Looks one named total up in a payslip the bureau delivered. The payroll
     * desk names the total in the query file it drops on the document centre,
     * so the same payslip can be checked for several totals in a run.
     */
    public String findTotal(HrDocument doc, String totalName) {
        try (java.io.InputStream in = getResource(doc).getInputStream()) {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document parsed = dbf.newDocumentBuilder().parse(in);
            javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
            String expr = "//total[@name='" + totalName + "']";
            org.w3c.dom.NodeList found = (org.w3c.dom.NodeList) xpath.evaluate(expr, parsed,
                    javax.xml.xpath.XPathConstants.NODESET);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < found.getLength(); i++) {
                sb.append(found.item(i).getTextContent()).append(' ');
            }
            return sb.toString().trim();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not find total in " + doc.getDisplayName(), e);
        }
    }

    public void recordDownload(HrDocument doc, EmployeePrincipal actor) {
        auditService.record(actor, "DOCUMENT_DOWNLOAD", "HrDocument", doc.getId().toString(),
                null, doc.getDisplayName(), "owner=" + doc.getEmployee().getId() + " actor=" + actor.getId());
    }
}
