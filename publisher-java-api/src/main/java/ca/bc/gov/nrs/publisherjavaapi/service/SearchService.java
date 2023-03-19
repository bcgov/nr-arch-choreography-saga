package ca.bc.gov.nrs.publisherjavaapi.service;

import ca.bc.gov.nrs.publisherjavaapi.filter.FilterOperation;
import ca.bc.gov.nrs.publisherjavaapi.filter.PermitFilterSpecs;
import ca.bc.gov.nrs.publisherjavaapi.model.Permit;
import ca.bc.gov.nrs.publisherjavaapi.struct.Condition;
import ca.bc.gov.nrs.publisherjavaapi.struct.Search;
import ca.bc.gov.nrs.publisherjavaapi.struct.SearchCriteria;
import ca.bc.gov.nrs.publisherjavaapi.struct.ValueType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

  /**
   * The type Student search service.
   */

  private final PermitFilterSpecs permitFilterSpecs;

  /**
   * Instantiates a new Student search service.
   *
   * @param permitFilterSpecs the student filter specs
   */
  public SearchService(PermitFilterSpecs permitFilterSpecs) {
    this.permitFilterSpecs = permitFilterSpecs;
  }

  /**
   * Gets specifications.
   *
   * @param studentSpecs the pen reg batch specs
   * @param i            the
   * @param search       the search
   * @return the specifications
   */
  public Specification<Permit> getSpecifications(Specification<Permit> studentSpecs, int i, Search search) {
    if (i == 0) {
      studentSpecs = getPermitSpecification(search.getSearchCriteriaList());
    } else {
      if (search.getCondition() == Condition.AND) {
        studentSpecs = studentSpecs.and(getPermitSpecification(search.getSearchCriteriaList()));
      } else {
        studentSpecs = studentSpecs.or(getPermitSpecification(search.getSearchCriteriaList()));
      }
    }
    return studentSpecs;
  }

  private Specification<Permit> getPermitSpecification(List<SearchCriteria> criteriaList) {
    Specification<Permit> permitSpecification = null;
    if (!criteriaList.isEmpty()) {
      int i = 0;
      for (SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          var criteriaValue = criteria.getValue();
          Specification<Permit> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteriaValue, criteria.getValueType());
          permitSpecification = getSpecificationPerGroup(permitSpecification, i, criteria, typeSpecification);
          i++;
        } else {
          throw new InvalidParameterException("Search Criteria can not contain null values for key, value and operation type");
        }
      }
    }
    return permitSpecification;
  }

  /**
   * Gets specification per group.
   *
   * @param permitSpecification the pen request batch entity specification
   * @param i                   the
   * @param criteria            the criteria
   * @param typeSpecification   the type specification
   * @return the specification per group
   */
  private Specification<Permit> getSpecificationPerGroup(Specification<Permit> permitSpecification, int i, SearchCriteria criteria, Specification<Permit> typeSpecification) {
    if (i == 0) {
      permitSpecification = Specification.where(typeSpecification);
    } else {
      if (criteria.getCondition() == Condition.AND) {
        permitSpecification = permitSpecification.and(typeSpecification);
      } else {
        permitSpecification = permitSpecification.or(typeSpecification);
      }
    }
    return permitSpecification;
  }

  private Specification<Permit> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType) {
    Specification<Permit> permitSpecification;
    switch (valueType) {
      case STRING -> permitSpecification = permitFilterSpecs.getStringTypeSpecification(key, value, filterOperation);
      case DATE_TIME -> permitSpecification = permitFilterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
      case LONG -> permitSpecification = permitFilterSpecs.getLongTypeSpecification(key, value, filterOperation);
      case INTEGER -> permitSpecification = permitFilterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
      case DATE -> permitSpecification = permitFilterSpecs.getDateTypeSpecification(key, value, filterOperation);
      case UUID -> permitSpecification = permitFilterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
      default -> throw new InvalidParameterException("Invalid value type");

    }
    return permitSpecification;
  }

  /**
   * Sets specification and sort criteria.
   *
   * @param sortCriteriaJson       the sort criteria json
   * @param searchCriteriaListJson the search criteria list json
   * @param objectMapper           the object mapper
   * @param sorts                  the sorts
   * @return the specification and sort criteria
   */
  public Specification<Permit> setSpecificationAndSortCriteria(String sortCriteriaJson, String searchCriteriaListJson, ObjectMapper objectMapper, List<Sort.Order> sorts) {
    Specification<Permit> studentSpecs = null;
    try {
      getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (null != searchCriteriaListJson && !searchCriteriaListJson.isEmpty()) {
        List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        int i = 0;
        for (var search : searches) {
          studentSpecs = getSpecifications(studentSpecs, i, search);
          i++;
        }
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage());
    }
    return studentSpecs;
  }

  public static void getSortCriteria(String sortCriteriaJson, ObjectMapper objectMapper, List<Sort.Order> sorts) throws JsonProcessingException {
    if (null != sortCriteriaJson && !sortCriteriaJson.isEmpty()) {
      Map<String, String> sortMap = objectMapper.readValue(sortCriteriaJson, new TypeReference<>() {
      });
      sortMap.forEach((k, v) -> {
        if ("ASC".equalsIgnoreCase(v)) {
          sorts.add(new Sort.Order(Sort.Direction.ASC, k));
        } else {
          sorts.add(new Sort.Order(Sort.Direction.DESC, k));
        }
      });
    }
  }
}
