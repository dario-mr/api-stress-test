package com.dario.ast.core.domain;

import static com.dario.ast.util.CopyUtil.deepCopy;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.dario.ast.view.component.sidebar.request.RequestOrFolder;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Request implements RequestOrFolder {

  private ConfigParams configParams;
  private RunParams runParams;
  private Folder folder;

  @Override
  @JsonProperty(access = READ_ONLY)
  public Long getId() {
    return configParams.getRequestId();
  }

  @Override
  @JsonProperty(access = READ_ONLY)
  public Folder getParentFolder() {
    return folder;
  }

  public void updateFrom(Request other) {
    this.configParams = other.configParams;
    this.runParams = other.runParams;
    this.folder = other.folder;
  }

  public Request duplicate() {
    return new Request(
        deepCopy(configParams, ConfigParams.class),
        deepCopy(runParams, RunParams.class),
        deepCopy(folder, Folder.class)
    );
  }
}
