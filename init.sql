CREATE TABLE IF NOT EXISTS task_job (
	id VARCHAR(255) PRIMARY KEY,
	status VARCHAR(32),
	payload VARCHAR(255),
	execute_time DATETIME
);


