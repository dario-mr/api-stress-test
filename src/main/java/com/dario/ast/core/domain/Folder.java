package com.dario.ast.core.domain;

import com.dario.ast.view.component.sidebar.request.RequestOrFolder;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public final class Folder implements RequestOrFolder {

  private Long id;
  private Long userId;
  private String name;
  private Instant createdOn;
  private Folder parentFolder;

  @Override
  public Folder getParentFolder() {
    return parentFolder;
  }

  public static Folder defaultFolder(long userId, Folder parentFolder) {
    return new Folder(null, userId, "New folder", Instant.now(), parentFolder);
  }

  public void updateFrom(Folder other) {
    this.id = other.id;
    this.userId = other.userId;
    this.name = other.name;
    this.createdOn = other.createdOn;
    this.parentFolder = other.parentFolder;
  }
}
