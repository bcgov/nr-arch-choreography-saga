package ca.bc.gov.nrs.publisherjavaapi.struct;


import ca.bc.gov.nrs.publisherjavaapi.filter.FilterOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
/**
 * The type Search criteria.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchCriteria {
  /**
   * The Key.
   */
  @NotNull
  String key;
  /**
   * The Operation.
   */
  @NotNull
  FilterOperation operation;
  /**
   * The Value.
   */
  String value;
  /**
   * The Value type.
   */
  @NotNull
  ValueType valueType;
  /**
   * The Condition. ENUM to hold and AND OR
   */
  Condition condition;
}
