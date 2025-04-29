package com.dario.ast.repository.jpa.entity;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ast_request", schema = "my_schema")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RequestEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "created_on", nullable = false)
  private Instant createdOn;

  @Column(name = "modified_on", nullable = false)
  private Instant modifiedOn;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "uri", nullable = false)
  private String uri;

  @Column(name = "method", nullable = false)
  private String httpMethod;

  @Column(name = "request_type", nullable = false)
  private String requestType;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "folder_id")
  @ToString.Exclude
  private RequestFolderEntity folder;

  @ElementCollection(fetch = EAGER)
  @MapKeyColumn(name = "header_key")
  @Column(name = "header_value")
  @CollectionTable(
      name = "ast_request_headers",
      schema = "my_schema",
      joinColumns = @JoinColumn(name = "ast_request_id"))
  @OrderBy("created_on ASC")
  private Map<String, RequestHeaderEntity> headers;

  @ElementCollection(fetch = EAGER)
  @MapKeyColumn(name = "variable_key")
  @Column(name = "variable_value")
  @CollectionTable(
      name = "ast_request_uri_variables",
      schema = "my_schema",
      joinColumns = @JoinColumn(name = "ast_request_id"))
  @OrderBy("created_on ASC")
  private Map<String, RequestUriVariableEntity> uriVariables;

  @ElementCollection(fetch = EAGER)
  @MapKeyColumn(name = "param_key")
  @Column(name = "param_value")
  @CollectionTable(
      name = "ast_request_query_params",
      schema = "my_schema",
      joinColumns = @JoinColumn(name = "ast_request_id"))
  @OrderBy("created_on ASC")
  private Map<String, RequestQueryParameterEntity> queryParams;

  @Column(name = "request_body", columnDefinition = "TEXT")
  private String requestBody;

  @Column(name = "num_requests", nullable = false)
  private int numRequests;

  @Column(name = "thread_pool_size", nullable = false)
  private int threadPoolSize;

  @Column(name = "stop_on_error", nullable = false)
  private boolean stopOnError;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestEntity that = (RequestEntity) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id == null ? 0 : id.hashCode();
  }
}
