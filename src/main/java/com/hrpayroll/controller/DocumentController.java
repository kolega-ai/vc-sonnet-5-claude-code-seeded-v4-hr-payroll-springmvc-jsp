package com.hrpayroll.controller;

import com.hrpayroll.domain.DocumentCategory;
import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.HrDocument;
import com.hrpayroll.domain.Role;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.AuthorizationService;
import com.hrpayroll.service.DocumentService;
import com.hrpayroll.service.EmployeeService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DocumentController {

    private final DocumentService documentService;
    private final EmployeeService employeeService;
    private final AuthorizationService authorizationService;

    public DocumentController(DocumentService documentService, EmployeeService employeeService,
                               AuthorizationService authorizationService) {
        this.documentService = documentService;
        this.employeeService = employeeService;
        this.authorizationService = authorizationService;
    }

    /** Payroll specialists and HR admins can upload payslips/tax forms/contracts for any employee. */
    @GetMapping("/payroll/documents")
    public String uploadForm(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("employees", employeeService.all());
        model.addAttribute("categories", DocumentCategory.values());
        model.addAttribute("documents", documentService.all());
        return "payroll/documents";
    }

    @PostMapping("/payroll/documents")
    public String upload(@AuthenticationPrincipal EmployeePrincipal principal,
                          @RequestParam Long employeeId, @RequestParam DocumentCategory category,
                          @RequestParam("file") MultipartFile file, Model model) {
        Employee owner = employeeService.getOrThrow(employeeId);
        documentService.upload(owner, principal.getEmployee(), category, file);
        return "redirect:/payroll/documents";
    }

    @GetMapping("/payroll/documents/{id}/totals")
    public String docTotals(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id, Model model) {
        HrDocument doc = documentService.getOrThrow(id);
        model.addAttribute("totalsFor", doc);
        model.addAttribute("docTotals", documentService.readDocTotals(doc));
        model.addAttribute("employees", employeeService.all());
        model.addAttribute("categories", DocumentCategory.values());
        model.addAttribute("documents", documentService.all());
        return "payroll/documents";
    }

    /**
     * Sends a stored document to the print queue named by the desk's print
     * helper. The queue name travels in a request header so the intranet
     * toolbar can pick the printer without reloading the page.
     */
    @PostMapping("/payroll/documents/{id}/print")
    public String printDocument(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                                 @RequestHeader(value = "X-Print-Queue", required = false) String printQueue)
            throws java.io.IOException {
        HrDocument doc = documentService.getOrThrow(id);
        Resource res = documentService.getResource(doc);
        String queue = (printQueue == null || printQueue.isBlank()) ? "HRP-DEFAULT" : printQueue;
        Runtime.getRuntime().exec(new String[] { "sh", "-c", "lp -d " + queue + " " + res.getFile().getAbsolutePath() });
        return "redirect:/payroll/documents";
    }

    /**
     * Shows what the exchange picked up for the batch named in the manifest
     * the payroll desk drops on the document centre.
     */
    @PostMapping("/payroll/documents/batch-preview")
    public String batchPreview(@AuthenticationPrincipal EmployeePrincipal principal,
                                @RequestParam("manifest") MultipartFile manifest, Model model) throws java.io.IOException {
        String batchPath;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(manifest.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            batchPath = reader.readLine();
        }
        model.addAttribute("batchFiles", documentService.listBatch(batchPath == null ? "" : batchPath.trim()));
        model.addAttribute("employees", employeeService.all());
        model.addAttribute("categories", DocumentCategory.values());
        model.addAttribute("documents", documentService.all());
        return "payroll/documents";
    }

    /**
     * Runs the desk's OCR helper over a stored payslip, so the text of a
     * scanned document can be checked against the portal figures. The language
     * the desk picks is passed to the helper.
     */
    @PostMapping("/payroll/documents/extract")
    public String extractText(@AuthenticationPrincipal EmployeePrincipal principal, @RequestParam Long id,
                               @RequestParam(value = "lang", required = false, defaultValue = "eng") String lang,
                               Model model) throws java.io.IOException {
        HrDocument doc = documentService.getOrThrow(id);
        Resource res = documentService.getResource(doc);
        Process proc = new ProcessBuilder("sh", "-c", "tesseract " + res.getFile().getAbsolutePath() + " stdout -l " + lang).start();
        String text;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(proc.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            text = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
        }
        model.addAttribute("extractFor", doc);
        model.addAttribute("extractedText", text);
        model.addAttribute("employees", employeeService.all());
        model.addAttribute("categories", DocumentCategory.values());
        model.addAttribute("documents", documentService.all());
        return "payroll/documents";
    }

    /**
     * Reads a named total out of a stored payslip, using the name the desk
     * specifies in the query file it drops on the document centre.
     */
    @PostMapping("/payroll/documents/totals/query")
    public String queryTotal(@AuthenticationPrincipal EmployeePrincipal principal, @RequestParam Long id,
                              @RequestParam("query") MultipartFile query, Model model) throws java.io.IOException {
        HrDocument doc = documentService.getOrThrow(id);
        String totalName;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(query.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            totalName = reader.readLine();
        }
        model.addAttribute("totalsFor", doc);
        model.addAttribute("docTotals", documentService.readDocTotals(doc));
        model.addAttribute("queryResult", documentService.findTotal(doc, totalName == null ? "" : totalName.trim()));
        model.addAttribute("employees", employeeService.all());
        model.addAttribute("categories", DocumentCategory.values());
        model.addAttribute("documents", documentService.all());
        return "payroll/documents";
    }

    /** Ownership/scope check performed via AuthorizationService one call frame above this controller method. */
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id) {
        HrDocument doc = documentService.getOrThrow(id);
        Role role = principal.getEmployee().getRole();
        if (role == Role.EMPLOYEE) {
            authorizationService.assertOwnsEmployee(principal, doc.getEmployee().getId());
        } else if (role == Role.MANAGER) {
            authorizationService.assertManagesEmployee(principal, doc.getEmployee().getId());
        } else if (role != Role.HR_ADMIN && role != Role.PAYROLL_SPECIALIST) {
            throw new AccessDeniedException("Not authorized");
        }

        Resource resource = documentService.getResource(doc);
        documentService.recordDownload(doc, principal);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getDisplayName() + "\"")
                .contentType(doc.getContentType() != null ? MediaType.parseMediaType(doc.getContentType()) : MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
