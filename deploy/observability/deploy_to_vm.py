#!/usr/bin/env python3
"""Deploy observability system to VMware Ubuntu VM"""
import paramiko
import os
import sys
import time

HOST = "192.168.44.128"
USER = "root"
PASSWORD = "123456"
LOCAL_DIR = os.path.dirname(os.path.abspath(__file__))
REMOTE_DIR = "/root/observability"

def ssh_connect():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, username=USER, password=PASSWORD, timeout=10)
    return client

def run_cmd(client, cmd, timeout=120):
    print(f"  >>> {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode('utf-8', errors='replace')
    err = stderr.read().decode('utf-8', errors='replace')
    if out:
        print(out.strip())
    if err:
        print(f"  [stderr] {err.strip()}")
    return out, err

def sftp_upload_dir(client, local_dir, remote_dir):
    """Upload entire directory recursively"""
    sftp = client.open_sftp()
    try:
        sftp.stat(remote_dir)
    except FileNotFoundError:
        sftp.mkdir(remote_dir)

    for item in os.listdir(local_dir):
        local_path = os.path.join(local_dir, item)
        remote_path = f"{remote_dir}/{item}"
        if item == "deploy_to_vm.py":
            continue
        if os.path.isdir(local_path):
            try:
                sftp.stat(remote_path)
            except FileNotFoundError:
                sftp.mkdir(remote_path)
            sftp_upload_dir(client, local_path, remote_path)
        else:
            print(f"  Uploading: {item}")
            sftp.put(local_path, remote_path)
    sftp.close()

def main():
    print("=" * 60)
    print("Deploying Observability System to VM")
    print(f"Target: {USER}@{HOST}")
    print("=" * 60)

    print("\n[1/6] Connecting to VM...")
    client = ssh_connect()
    print("  Connected!")

    print("\n[2/6] Checking system...")
    run_cmd(client, "uname -a")
    run_cmd(client, "free -h | head -2")

    print("\n[3/6] Installing Docker if needed...")
    out, _ = run_cmd(client, "docker --version 2>&1 || echo 'DOCKER_NOT_FOUND'")
    if "DOCKER_NOT_FOUND" in out:
        print("  Installing Docker...")
        run_cmd(client, "curl -fsSL https://get.docker.com | bash", timeout=300)
        run_cmd(client, "systemctl enable docker && systemctl start docker")
        run_cmd(client, "docker --version")
    else:
        print("  Docker already installed")

    # Check docker-compose
    out, _ = run_cmd(client, "docker compose version 2>&1 || docker-compose --version 2>&1 || echo 'COMPOSE_NOT_FOUND'")
    if "COMPOSE_NOT_FOUND" in out:
        print("  Installing docker-compose plugin...")
        run_cmd(client, "apt-get update -qq && apt-get install -y -qq docker-compose-v2 2>&1 | tail -3", timeout=180)
    else:
        print("  Docker Compose available")

    print("\n[4/6] Uploading config files...")
    run_cmd(client, f"mkdir -p {REMOTE_DIR}")
    sftp_upload_dir(client, LOCAL_DIR, REMOTE_DIR)
    print("  Upload complete!")

    print("\n[5/6] Deploying services...")
    run_cmd(client, f"cd {REMOTE_DIR} && docker compose down --remove-orphans 2>&1 || true")
    run_cmd(client, f"cd {REMOTE_DIR} && docker compose up -d 2>&1", timeout=300)

    print("\n[6/6] Verifying deployment...")
    time.sleep(5)
    run_cmd(client, f"cd {REMOTE_DIR} && docker compose ps")

    print("\n" + "=" * 60)
    print("DEPLOYMENT COMPLETE!")
    print("=" * 60)
    print("Access URLs:")
    print(f"  Grafana:      http://{HOST}:3000 (admin/admin)")
    print(f"  Prometheus:   http://{HOST}:9090")
    print(f"  Jaeger:       http://{HOST}:16686")
    print(f"  MinIO:        http://{HOST}:9001")
    print(f"  Alloy:        http://{HOST}:12345")
    print("=" * 60)

    client.close()

if __name__ == "__main__":
    main()