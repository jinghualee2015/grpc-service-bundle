package com.nyquistdata.grpc.api.resource.impl;

import com.nyquistdata.grpc.api.exceptions.NestedIoException;
import com.nyquistdata.grpc.api.resource.Resource;
import com.nyquistdata.grpc.api.utils.VfsUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/29
 * @description:
 */
public class VfsResource extends AbstractResource{
    private final Object resource;

    /**
     * Create a new {@code VfsResource} wrapping the given resource handle.
     *
     * @param resource a {@code org.jboss.vfs.VirtualFile} instance
     *                 (untyped in order to avoid a static dependency on the VFS API)
     */
    public VfsResource(Object resource) {
        this.resource = resource;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return VfsUtils.getInputStream(this.resource);
    }

    @Override
    public boolean exists() {
        return VfsUtils.exists(this.resource);
    }

    @Override
    public boolean isReadable() {
        return VfsUtils.isReadable(this.resource);
    }

    @Override
    public URL getUrl() throws IOException {
        try {
            return VfsUtils.getUrl(this.resource);
        } catch (Exception ex) {
            throw new NestedIoException("Failed to obtain URL for file " + this.resource, ex);
        }
    }

    @Override
    public URI getUri() throws IOException {
        try {
            return VfsUtils.getUri(this.resource);
        } catch (Exception ex) {
            throw new NestedIoException("Failed to obtain URI for " + this.resource, ex);
        }
    }

    @Override
    public File getFile() throws IOException {
        return VfsUtils.getFile(this.resource);
    }

    @Override
    public long contentLength() throws IOException {
        return VfsUtils.getSize(this.resource);
    }

    @Override
    public long lastModified() throws IOException {
        return VfsUtils.getLastModified(this.resource);
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        if (!relativePath.startsWith(".") && relativePath.contains("/")) {
            try {
                return new VfsResource(VfsUtils.getChild(this.resource, relativePath));
            } catch (IOException ex) {
                // fall back to getRelative
            }
        }

        return new VfsResource(VfsUtils.getRelative(new URL(getUrl(), relativePath)));
    }

    @Override
    public String getFilename() {
        return VfsUtils.getName(this.resource);
    }

    @Override
    public String getDescription() {
        return "VFS resource [" + this.resource + "]";
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof VfsResource
                && this.resource.equals(((VfsResource) other).resource)));
    }

    @Override
    public int hashCode() {
        return this.resource.hashCode();
    }
}
