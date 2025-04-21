package com.dario.ast.core.domain;

import com.dario.ast.view.component.sidebar.request.RequestOrFolder;
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
}
