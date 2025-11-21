# Podman Setup Guide for EadgeQuery-AI

Complete guide for setting up and running EadgeQuery microservices with Podman instead of Docker.

---

## Table of Contents
1. [Why Podman?](#why-podman)
2. [Podman vs Docker](#podman-vs-docker)
3. [Installation](#installation)
4. [Rootless vs Rootful Podman](#rootless-vs-rootful-podman)
5. [Configuration for Your Setup](#configuration-for-your-setup)
6. [Running the Stack](#running-the-stack)
7. [Troubleshooting](#troubleshooting)

---

## Why Podman?

**Podman** is a daemonless container engine for developing, managing, and running OCI Containers on your Linux System.

**Benefits:**
- ✅ **Daemonless**: No background daemon required (more secure)
- ✅ **Rootless**: Can run containers without root privileges
- ✅ **Docker-compatible**: Uses same Dockerfile and compose formats
- ✅ **More secure**: Better security model and isolation
- ✅ **Systemd integration**: Native systemd support for containers
- ✅ **Pod support**: Native Kubernetes pod concept

---

## Podman vs Docker

| Feature | Docker | Podman |
|---------|--------|--------|
| Daemon | Required (dockerd) | Daemonless |
| Root privileges | Usually needs root | Can run rootless |
| Architecture | Client-server | Fork-exec model |
| Systemd integration | Third-party | Native |
| Docker CLI compatibility | N/A | 100% compatible |
| Kubernetes YAML | No | Yes (pods) |

---

## Installation

### Ubuntu/Debian
```bash
# Update package list
sudo apt update

# Install Podman
sudo apt install -y podman

# Install podman-compose
pip3 install podman-compose

# Verify installation
podman --version
podman-compose --version
```

### Fedora/RHEL/CentOS
```bash
# Podman comes pre-installed on Fedora 33+
sudo dnf install -y podman

# Install podman-compose
pip3 install podman-compose

# Verify installation
podman --version
podman-compose --version
```

### Arch Linux
```bash
sudo pacman -S podman
pip3 install podman-compose
```

---

## Rootless vs Rootful Podman

### Rootless Podman (Recommended for Development)

**What is it?**
- Containers run as your regular user (no root privileges)
- More secure, better for development
- Default on most modern systems

**Characteristics:**
- Socket location: `${XDG_RUNTIME_DIR}/podman/podman.sock` (typically `/run/user/1000/podman/podman.sock`)
- Network: Uses slirp4netns (slightly slower, but secure)
- Ports: Can't bind to ports < 1024 without special configuration
- Volumes: SELinux labels (`:Z` or `:z`) are important

**Check if you're running rootless:**
```bash
podman info --format "{{.Host.Security.Rootless}}"
# Output: true = rootless, false = rootful
```

### Rootful Podman (Root Privileges)

**What is it?**
- Containers run with root privileges
- More Docker-like behavior
- Needed for some advanced features

**Characteristics:**
- Socket location: `/run/podman/podman.sock`
- Network: Can use bridge networking (like Docker)
- Ports: Can bind to any port
- Volumes: More permissive, but still benefits from SELinux labels

**Run rootful Podman:**
```bash
sudo podman-compose up -d
```

---

## Configuration for Your Setup

### Step 1: Determine Your Podman Mode

```bash
# Check if rootless
podman info --format "{{.Host.Security.Rootless}}"

# Check socket location
podman info --format "{{.Host.RemoteSocket.Path}}"
```

### Step 2: Update docker-compose.yml for Your Mode

#### For Rootless Podman Users

Edit `docker-compose.yml` and update the Jenkins service socket path:

```yaml
# Change line 297 from:
- /run/podman/podman.sock:/var/run/docker.sock:Z

# To (replace 1000 with your user ID from 'echo $UID'):
- /run/user/1000/podman/podman.sock:/var/run/docker.sock:Z

# Or use environment variable:
- ${XDG_RUNTIME_DIR}/podman/podman.sock:/var/run/docker.sock:Z
```

**Also, for rootless networking, update line 326:**
```yaml
# Comment out or remove the bridge driver:
networks:
  edagequry-net:
    # driver: bridge  # Not needed for rootless
```

#### For Rootful Podman Users

The configuration is already set for rootful Podman. Just ensure you run commands with `sudo`:

```bash
sudo podman-compose up -d
```

### Step 3: Enable Podman Socket (Optional, for Jenkins/CI)

#### Rootless Socket:
```bash
# Enable user podman socket
systemctl --user enable --now podman.socket

# Check status
systemctl --user status podman.socket

# Verify socket exists
ls -la ${XDG_RUNTIME_DIR}/podman/podman.sock
```

#### Rootful Socket:
```bash
# Enable system podman socket
sudo systemctl enable --now podman.socket

# Check status
sudo systemctl status podman.socket

# Verify socket exists
sudo ls -la /run/podman/podman.sock
```

### Step 4: Configure SELinux (if applicable)

If you're on RHEL/Fedora/CentOS with SELinux enabled:

```bash
# Check if SELinux is enabled
getenforce

# If 'Enforcing', volumes need :Z or :z labels
# :Z = private unshared label
# :z = shared label

# Already configured in docker-compose.yml with :Z labels
```

---

## Running the Stack

### Starting All Services

```bash
# Rootless mode (recommended for development)
podman-compose up -d

# Rootful mode (if you need root privileges)
sudo podman-compose up -d
```

### Starting Specific Services

```bash
# Start only databases
podman-compose up -d mysql mysql-user-profile mysql-datasource mysql-chatbot

# Start SonarQube stack
podman-compose up -d sonarqube-db sonarqube

# Start messaging stack
podman-compose up -d zookeeper kafka

# Start Jenkins
podman-compose up -d jenkins
```

### Viewing Logs

```bash
# All services
podman-compose logs -f

# Specific service
podman-compose logs -f sonarqube

# Last 100 lines
podman-compose logs --tail=100 jenkins
```

### Stopping Services

```bash
# Stop all
podman-compose down

# Stop without removing volumes
podman-compose stop

# Stop specific service
podman-compose stop jenkins
```

### Building Services

```bash
# Build all custom services
podman-compose build

# Build specific service
podman-compose build auth

# Build without cache
podman-compose build --no-cache
```

---

## Troubleshooting

### Issue 1: Permission Denied on Socket

**Error:**
```
Error: unable to connect to Podman socket
```

**Solution (Rootless):**
```bash
# Enable and start socket
systemctl --user enable --now podman.socket

# Verify
systemctl --user status podman.socket

# Update docker-compose.yml socket path
# Use: ${XDG_RUNTIME_DIR}/podman/podman.sock
```

**Solution (Rootful):**
```bash
# Enable system socket
sudo systemctl enable --now podman.socket

# Add your user to podman group (optional)
sudo usermod -aG podman $USER
newgrp podman
```

### Issue 2: Network Errors (rootless)

**Error:**
```
Error: unable to create network
```

**Solution:**
```bash
# Remove bridge driver from docker-compose.yml
# Edit line 326 and remove or comment:
# driver: bridge

# Or use rootful mode
sudo podman-compose up -d
```

### Issue 3: Port Binding Failed (ports < 1024)

**Error:**
```
Error: cannot bind to port 80: permission denied
```

**Solution (Rootless):**
```bash
# Allow rootless to bind to low ports
sudo sysctl net.ipv4.ip_unprivileged_port_start=80

# Make persistent
echo 'net.ipv4.ip_unprivileged_port_start=80' | sudo tee /etc/sysctl.d/podman-ports.conf

# Or use port mapping (recommended)
# Change 80:80 to 8080:80 in docker-compose.yml
```

### Issue 4: SELinux Denials

**Error:**
```
Error: Permission denied (SELinux)
```

**Solution:**
```bash
# Check SELinux status
getenforce

# Ensure :Z labels on volume mounts (already done in docker-compose.yml)
# Example: - ./data:/data:Z

# If issues persist, check audit log
sudo ausearch -m avc -ts recent

# Temporarily disable (not recommended for production)
sudo setenforce 0
```

### Issue 5: Containers Not Starting

**Debug:**
```bash
# Check container logs
podman logs <container-name>

# Check podman events
podman events

# Inspect container
podman inspect <container-name>

# Check healthcheck
podman healthcheck run <container-name>
```

### Issue 6: podman-compose Not Found

**Solution:**
```bash
# Install with pip
pip3 install podman-compose

# Or use pip with user flag
pip3 install --user podman-compose

# Add to PATH if needed
export PATH="$HOME/.local/bin:$PATH"
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
```

### Issue 7: "No module named 'dotenv'"

**Solution:**
```bash
pip3 install python-dotenv
```

### Issue 8: Healthcheck Conditions Not Working

**Note:** podman-compose has limited support for `condition: service_healthy` in `depends_on`.

**Solution:**
- The compose file already includes `restart: unless-stopped` for affected services
- Services will automatically restart if they fail due to dependency issues
- For manual control, start services in order:
  ```bash
  # Start databases first
  podman-compose up -d mysql mysql-user-profile

  # Wait for healthy
  podman healthcheck run mysql-auth

  # Start dependent services
  podman-compose up -d auth
  ```

---

## Useful Podman Commands

### Container Management
```bash
# List running containers
podman ps

# List all containers
podman ps -a

# Stop container
podman stop <container-name>

# Remove container
podman rm <container-name>

# Remove all stopped containers
podman container prune
```

### Image Management
```bash
# List images
podman images

# Remove image
podman rmi <image-name>

# Remove unused images
podman image prune

# Build image
podman build -t myimage:tag .
```

### Network Management
```bash
# List networks
podman network ls

# Inspect network
podman network inspect edagequry-net

# Create network
podman network create mynetwork

# Remove network
podman network rm mynetwork
```

### Volume Management
```bash
# List volumes
podman volume ls

# Inspect volume
podman volume inspect <volume-name>

# Remove volume
podman volume rm <volume-name>

# Remove all unused volumes
podman volume prune
```

### System Information
```bash
# System info
podman info

# System resources usage
podman system df

# Clean up everything
podman system prune -a --volumes
```

---

## Migration from Docker

If you're migrating from Docker:

```bash
# Export Docker image
docker save myimage:tag > myimage.tar

# Import to Podman
podman load < myimage.tar

# Or use alias (Podman is Docker-compatible)
alias docker=podman
```

**Add to your shell profile:**
```bash
echo 'alias docker=podman' >> ~/.bashrc
echo 'alias docker-compose=podman-compose' >> ~/.bashrc
source ~/.bashrc
```

---

## Best Practices

1. **Use rootless for development**: More secure, easier to manage
2. **Enable auto-updates**: `podman auto-update` for production
3. **Use systemd services**: Convert compose files to systemd units for production
4. **Monitor resources**: Use `podman stats` to monitor container resources
5. **Regular cleanup**: Run `podman system prune` periodically
6. **Use SELinux labels**: Always add `:Z` to volume mounts on SELinux systems
7. **Health checks**: Implement proper health checks in your Dockerfiles
8. **Resource limits**: Set memory and CPU limits for containers

---

## Quick Reference

### Common Operations

| Task | Command |
|------|---------|
| Start all services | `podman-compose up -d` |
| Stop all services | `podman-compose down` |
| View logs | `podman-compose logs -f` |
| Rebuild service | `podman-compose build <service>` |
| Restart service | `podman-compose restart <service>` |
| Shell into container | `podman exec -it <container> bash` |
| Check resource usage | `podman stats` |
| Cleanup system | `podman system prune -a` |

### Access URLs (Same as Docker)

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8765
- **Zipkin UI**: http://localhost:9411
- **SonarQube**: http://localhost:9000
- **Jenkins**: http://localhost:8982/jenkins
- **phpMyAdmin (Auth)**: http://localhost:8080
- **phpMyAdmin (Profile)**: http://localhost:8083
- **phpMyAdmin (Datasource)**: http://localhost:8084
- **phpMyAdmin (Chatbot)**: http://localhost:8085

---

## Additional Resources

- **Podman Documentation**: https://docs.podman.io
- **Podman Compose GitHub**: https://github.com/containers/podman-compose
- **Podman Desktop**: https://podman-desktop.io (GUI alternative)
- **Migration Guide**: https://podman.io/getting-started/migration

---

## Need Help?

1. Check Podman info: `podman info`
2. Check logs: `podman-compose logs <service>`
3. Check system events: `podman events --since 5m`
4. Consult documentation: `man podman` or `man podman-compose`
5. Open an issue in the repository

---

**Ready to start?**

```bash
# Quick start for rootless Podman
podman-compose up -d

# Check status
podman-compose ps

# View logs
podman-compose logs -f
```
