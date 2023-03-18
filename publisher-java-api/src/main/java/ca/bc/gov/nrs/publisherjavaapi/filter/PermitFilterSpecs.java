package ca.bc.gov.nrs.publisherjavaapi.filter;

import ca.bc.gov.nrs.publisherjavaapi.model.Permit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

/**
 * The type Student filter specs.
 */
@Service
@Slf4j
public class PermitFilterSpecs extends BaseFilterSpecs<Permit> {

  /**
   * Instantiates a new Student filter specs.
   *
   * @param dateFilterSpecifications     the date filter specifications
   * @param dateTimeFilterSpecifications the date time filter specifications
   * @param integerFilterSpecifications  the integer filter specifications
   * @param stringFilterSpecifications   the string filter specifications
   * @param longFilterSpecifications     the long filter specifications
   * @param uuidFilterSpecifications     the uuid filter specifications
   * @param converters                   the converters
   */
  public PermitFilterSpecs(FilterSpecifications<Permit, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<Permit, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<Permit, Integer> integerFilterSpecifications, FilterSpecifications<Permit, String> stringFilterSpecifications, FilterSpecifications<Permit, Long> longFilterSpecifications, FilterSpecifications<Permit, UUID> uuidFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, converters);
  }
}
