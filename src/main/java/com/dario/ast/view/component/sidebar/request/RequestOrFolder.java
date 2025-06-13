package com.dario.ast.view.component.sidebar.request;

import com.dario.ast.core.domain.Folder;

public interface RequestOrFolder {

  Long getId();

  Folder getParentFolder();

}
