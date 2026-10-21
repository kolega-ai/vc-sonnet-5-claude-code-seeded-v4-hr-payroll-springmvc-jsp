create table departments (
    id bigserial primary key,
    name varchar(120) not null unique,
    description varchar(500)
);

create table employees (
    id bigserial primary key,
    email varchar(190) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(120) not null,
    role varchar(30) not null,
    department_id bigint references departments(id),
    manager_id bigint references employees(id),
    employment_status varchar(30) not null default 'ACTIVE',
    start_date date,
    phone varchar(30),
    address varchar(255),
    job_title varchar(120),
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);
create index idx_employees_manager on employees(manager_id);
create index idx_employees_department on employees(department_id);

create table compensation_requests (
    id bigserial primary key,
    employee_id bigint not null references employees(id),
    requested_by_id bigint not null references employees(id),
    proposed_salary numeric(14,2) not null,
    proposed_bonus numeric(14,2) default 0,
    effective_date date not null,
    justification varchar(1000),
    status varchar(30) not null default 'DRAFT',
    manager_approved_by_id bigint references employees(id),
    hr_approved_by_id bigint references employees(id),
    rejection_reason varchar(1000),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
create index idx_comp_requests_employee on compensation_requests(employee_id);
create index idx_comp_requests_status on compensation_requests(status);

-- Append-only: no application code ever issues UPDATE/DELETE against this table.
create table compensation_records (
    id bigserial primary key,
    employee_id bigint not null references employees(id),
    salary numeric(14,2) not null,
    bonus numeric(14,2) default 0,
    effective_date date not null,
    source_request_id bigint references compensation_requests(id),
    created_at timestamptz not null default now()
);
create index idx_comp_records_employee on compensation_records(employee_id);

create table onboarding_tasks (
    id bigserial primary key,
    assignee_id bigint not null references employees(id),
    title varchar(160) not null,
    description varchar(1000),
    due_date date,
    status varchar(30) not null default 'NOT_STARTED',
    evidence_document_id bigint,
    review_notes varchar(1000),
    reviewed_by_id bigint references employees(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
create index idx_onboarding_assignee on onboarding_tasks(assignee_id);
create index idx_onboarding_status on onboarding_tasks(status);

create table hr_documents (
    id bigserial primary key,
    employee_id bigint not null references employees(id),
    category varchar(30) not null,
    display_name varchar(200) not null,
    stored_filename varchar(200) not null,
    content_type varchar(120),
    file_size bigint not null default 0,
    uploaded_by_id bigint not null references employees(id),
    uploaded_at timestamptz not null default now()
);
create index idx_documents_employee on hr_documents(employee_id);

create table hr_requests (
    id bigserial primary key,
    employee_id bigint not null references employees(id),
    subject varchar(160) not null,
    description varchar(2000),
    status varchar(30) not null default 'OPEN',
    assigned_to_id bigint references employees(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
create index idx_hr_requests_employee on hr_requests(employee_id);

create table hr_request_comments (
    id bigserial primary key,
    hr_request_id bigint not null references hr_requests(id),
    author_id bigint not null references employees(id),
    body varchar(2000) not null,
    internal_only boolean not null default false,
    created_at timestamptz not null default now()
);
create index idx_hr_comments_request on hr_request_comments(hr_request_id);

create table policies (
    id bigserial primary key,
    title varchar(160) not null,
    version int not null default 1,
    content varchar(4000),
    audience varchar(60) not null default 'ALL',
    active boolean not null default true,
    created_at timestamptz not null default now()
);

create table policy_acknowledgements (
    id bigserial primary key,
    policy_id bigint not null references policies(id),
    employee_id bigint not null references employees(id),
    policy_version int not null,
    acknowledged_at timestamptz not null default now(),
    unique (policy_id, employee_id)
);
create index idx_acks_employee on policy_acknowledgements(employee_id);

-- Append-only audit trail; no update/delete path exists in application code.
create table audit_events (
    id bigserial primary key,
    actor_id bigint,
    actor_email varchar(190),
    action varchar(80) not null,
    resource_type varchar(80),
    resource_id varchar(80),
    before_value varchar(2000),
    after_value varchar(2000),
    authorization_basis varchar(300),
    timestamp timestamptz not null default now()
);
create index idx_audit_timestamp on audit_events(timestamp desc);

create table notifications (
    id bigserial primary key,
    recipient_id bigint not null references employees(id),
    message varchar(200) not null,
    link varchar(300),
    read_flag boolean not null default false,
    created_at timestamptz not null default now()
);
create index idx_notifications_recipient on notifications(recipient_id);

create table password_reset_tokens (
    id bigserial primary key,
    employee_id bigint not null references employees(id),
    token varchar(128) not null unique,
    expires_at timestamptz not null,
    used boolean not null default false,
    created_at timestamptz not null default now()
);

create table outbox_emails (
    id bigserial primary key,
    recipient varchar(190) not null,
    subject varchar(200) not null,
    body varchar(4000),
    sent_at timestamptz not null default now()
);
