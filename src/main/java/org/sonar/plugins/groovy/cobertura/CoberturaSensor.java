/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.groovy.cobertura;

import org.slf4j.LoggerFactory;
import org.sonar.api.Plugins;
import org.sonar.api.batch.AbstractCoverageExtension;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.cobertura.api.AbstractCoberturaParser;
import org.sonar.plugins.cobertura.api.CoberturaUtils;
import org.sonar.plugins.groovy.foundation.Groovy;
import org.sonar.plugins.groovy.foundation.GroovyFile;

import java.io.File;

/**
 * TODO copied from sonar-cobertura-plugin with modifications: JavaFile replaced by GroovyFile, fixed SONARPLUGINS-696
 */
public class CoberturaSensor extends AbstractCoverageExtension implements Sensor, DependsUponMavenPlugin {

  private CoberturaMavenPluginHandler handler;

  public CoberturaSensor(Plugins plugins, CoberturaMavenPluginHandler handler) {
    super(plugins);
    this.handler = handler;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return project.getAnalysisType().isDynamic(true) && Groovy.KEY.equals(project.getLanguageKey());
  }

  public void analyse(Project project, SensorContext context) {
    File report = CoberturaUtils.getReport(project);
    if (report != null) {
      parseReport(report, context);
    }
  }

  public MavenPluginHandler getMavenPluginHandler(Project project) {
    if (project.getAnalysisType().equals(Project.AnalysisType.DYNAMIC)) {
      return handler;
    }
    return null;
  }

  protected void parseReport(File xmlFile, final SensorContext context) {
    LoggerFactory.getLogger(CoberturaSensor.class).info("parsing {}", xmlFile);
    new AbstractCoberturaParser() {
      @Override
      protected Resource<?> getResource(String fileName) {
        return new GroovyFile(fileName);
      }
    }.parseReport(xmlFile, context);
  }

  @Override
  public String toString() {
    return "Groovy CoberturaSensor";
  }
}