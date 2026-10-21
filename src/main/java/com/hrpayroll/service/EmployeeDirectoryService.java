package com.hrpayroll.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Precision baseline primitive #4: a parameterised query whose sort column is
 * restricted to a whitelist before being concatenated into ORDER BY. All
 * filter VALUES remain bound parameters; only the column NAME is chosen from
 * a fixed, closed Set<String>, never taken verbatim from client input.
 */
@Service
public class EmployeeDirectoryService {

    private static final Set<String> SORTABLE_COLUMNS = Set.of(
            "full_name", "email", "job_title", "start_date", "employment_status");

    private final JdbcTemplate jdbcTemplate;

    public EmployeeDirectoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> listSorted(String sortColumn, String direction, Long departmentId) {
        String column = SORTABLE_COLUMNS.contains(sortColumn) ? sortColumn : "full_name";
        String dir = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        String sql = "select id, full_name, email, job_title, start_date, employment_status " +
                "from employees where (? is null or department_id = ?) order by " + column + " " + dir;
        return jdbcTemplate.queryForList(sql, departmentId, departmentId);
    }

    /**
     * Reads one person's entry from the corporate staff directory so HR can
     * check the directory details the org-change import is based on.
     */
    public Map<String, String> staffEntry(String uid) {
        Map<String, String> entry = new LinkedHashMap<>();
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(javax.naming.Context.PROVIDER_URL, "ldap://directory.hrpayroll.corp:389");
            javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext(env);
            String filter = "(&(objectClass=person)(uid=" + uid + "))";
            javax.naming.directory.SearchControls controls = new javax.naming.directory.SearchControls();
            javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> results =
                    ctx.search("ou=staff,dc=hrpayroll,dc=corp", filter, controls);
            while (results.hasMore()) {
                javax.naming.directory.SearchResult sr = results.next();
                entry.put("name", String.valueOf(sr.getAttributes().get("cn").get()));
                entry.put("mail", String.valueOf(sr.getAttributes().get("mail").get()));
            }
            ctx.close();
        } catch (Exception e) {
            entry.put("error", e.getMessage());
        }
        return entry;
    }
}
