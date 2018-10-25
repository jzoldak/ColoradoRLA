/** @copyright 2017 Colorado Department of State  **/

package us.freeandfair.corla.persistence;

import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.reflect.TypeToken;

/**
 * A converter between any HashMap and the serialized string in the db.
 *
 * @author Democracy Works, Inc. <dev@democracy.works>
 */
@Converter
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class HashMapAssignmentConverter
    implements AttributeConverter<Map<String, String>, String> {
  /**
   * The required type information.
   */
  private static final Type HASH_MAP_ASSIGNMENT_TYPE =
      new TypeToken<Map<String, String>>() { }.getType();

  /**
   * Our Gson instance, which does not do pretty-printing (unlike the global
   * one defined in Main).
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Convert the type into JSON for database storage.
   *
   * @param l the list to persist.
   */
  @Override
  public String convertToDatabaseColumn(final Map<String, String> l) {
    return GSON.toJson(l);
  }

  /**
   * Converts a type stored as JSON in the database to a Java object.
   *
   * @param s the JSON-encoded string
   */
  @Override
  public Map<String, String> convertToEntityAttribute(final String s) {
    return GSON.fromJson(s, HASH_MAP_ASSIGNMENT_TYPE);
  }
}
