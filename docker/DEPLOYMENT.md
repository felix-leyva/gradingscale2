# WasmJS Deployment Guide

This guide explains how to deploy the GradingScale WasmJS app to your Ubuntu server using Docker.

## Prerequisites

- Ubuntu server with Docker and Docker Compose installed
- Nginx Proxy Manager configured
- Domain name (optional)
- Firebase project (optional, for analytics and auth)

## Deployment Steps

### 1. Build the WasmJS Application

On your development machine:

```bash
# Build WasmJS distribution (official Kotlin approach)
./build-wasmjs.sh
```

This script simply runs:
```bash
./gradlew :composeApp:wasmJsBrowserDistribution
```

The build will:
- Generate production-ready WasmJS artifacts in `composeApp/build/dist/wasmJs/productionExecutable/`
- Copy files to `docker/wasmjs-app/`
- Prepare everything for Docker deployment

**Reference:** [Kotlin WasmJS Documentation](https://kotlinlang.org/docs/wasm-get-started.html#generate-artifacts)

### 2. Configure Firebase (Optional)

If you want Firebase analytics and authentication:

1. Copy the Firebase config template:
   ```bash
   cp composeApp/src/jsMain/resources/firebase-config.js.template docker/wasmjs-app/firebase-config.js
   ```

2. Edit `docker/wasmjs-app/firebase-config.js` with your Firebase project settings

3. **Important**: Never commit `firebase-config.js` to version control!

### 3. Transfer Files to Server

Copy the entire `docker/` directory to your Ubuntu server:

```bash
# Using rsync (recommended)
rsync -avz docker/ user@your-server:/path/to/gradingscale/docker/

# Or using scp
scp -r docker/ user@your-server:/path/to/gradingscale/
```

### 4. Deploy with Docker Compose

On your Ubuntu server:

```bash
cd /path/to/gradingscale/docker

# Create the external network if it doesn't exist
docker network create web

# Build and start the container
docker-compose up -d

# Check logs
docker-compose logs -f gradingscale-web
```

### 5. Configure Nginx Proxy Manager

In your Nginx Proxy Manager web interface:

1. Add a new Proxy Host
2. Details:
   - Domain Names: `gradingscale.yourdomain.com`
   - Scheme: `http`
   - Forward Hostname/IP: `gradingscale-wasmjs` (container name)
   - Forward Port: `80`
3. SSL:
   - Request a new SSL certificate with Let's Encrypt
   - Force SSL

### 6. Alternative: Direct Port Access

If you don't want to use Nginx Proxy Manager, the app is accessible at:
```
http://your-server-ip:8082
```

## Updating the Application

To update the app with new changes:

```bash
# On development machine
./build-wasmjs.sh
rsync -avz docker/wasmjs-app/ user@your-server:/path/to/gradingscale/docker/wasmjs-app/

# On server
cd /path/to/gradingscale/docker
docker-compose restart gradingscale-web
```

## Troubleshooting

### Check container logs
```bash
docker logs gradingscale-wasmjs
```

### Verify WASM MIME type
```bash
curl -I http://localhost:8082/your-app.wasm
# Should show: Content-Type: application/wasm
```

### Container won't start
- Check if port 8082 is already in use
- Verify file permissions in wasmjs-app directory

### Firebase not working
- Check browser console for errors
- Ensure firebase-config.js is present and properly configured
- Verify your Firebase project allows your domain

## Security Considerations

1. Always use HTTPS in production (handled by Nginx Proxy Manager)
2. Keep Firebase credentials secure
3. Regular updates of base nginx image
4. Consider adding rate limiting in nginx configuration