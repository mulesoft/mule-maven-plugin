/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model;

import org.apache.commons.lang.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;


public class ArtifactDescription {

    private final String COORDINATES_SEPARATOR = ":";
    private String groupId;
    private String artifactId;
    private String version;
    private String type;

    public ArtifactDescription() {

    }

    public ArtifactDescription(String artifactCoordinates) {
        checkArgument(StringUtils.isNotEmpty(artifactCoordinates), "The artifact coordinates must not be null nor empty");
        String[] coordinateElements = artifactCoordinates.split(COORDINATES_SEPARATOR);
        checkState(coordinateElements.length == 4, "The artifact coordinates are not in the format groupId:artifactId:version:type");
        setGroupId(coordinateElements[0]);
        setArtifactId(coordinateElements[1]);
        setVersion(coordinateElements[2]);
        setType(coordinateElements[3]);
    }

    public ArtifactDescription(String groupId, String artifactId, String version, String type) {
        setGroupId(groupId);
        setArtifactId(artifactId);
        setVersion(version);
        setType(type);
    }

    public String toString() {
        return format("%s:%s:%s:%s", groupId, artifactId, version, type);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        checkArgument(StringUtils.isNotEmpty(groupId), "The groupId must not be null nor empty");
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        checkArgument(StringUtils.isNotEmpty(artifactId), "The artifactId must not be null nor empty");
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        checkArgument(StringUtils.isNotEmpty(version), "The version must not be null nor empty");
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        checkArgument(StringUtils.isNotEmpty(version), "The type must not be null nor empty");
        this.type = type;
    }

    public String getContentDirectory() {
        if ("mule-standalone".equals(artifactId)) {
            return "mule-standalone-" + version;
        } else {
            return "mule-enterprise-standalone-" + version;
        }
    }
}

