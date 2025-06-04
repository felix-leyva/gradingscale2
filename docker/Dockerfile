FROM nginx:alpine

# Install any additional tools if needed
RUN apk add --no-cache curl

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the WasmJS app files
COPY wasmjs-app /usr/share/nginx/html

# Ensure proper permissions
RUN chmod -R 755 /usr/share/nginx/html

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]