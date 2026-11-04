package com.example

import com.example.data.model.CoordinateParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun coordinateParser_parsesValidCoordinates() {
    val result1 = CoordinateParser.parse("37.7749, -122.4194")
    assertNotNull(result1)
    assertEquals(37.7749, result1!!.first, 0.0001)
    assertEquals(-122.4194, result1.second, 0.0001)

    val result2 = CoordinateParser.parse("40.7128 -74.0060")
    assertNotNull(result2)
    assertEquals(40.7128, result2!!.first, 0.0001)
    assertEquals(-74.0060, result2.second, 0.0001)
  }

  @Test
  fun coordinateParser_rejectsInvalidBounds() {
    val invalidLat = CoordinateParser.parse("120.5, 45.0")
    assertNull(invalidLat)

    val invalidLng = CoordinateParser.parse("37.77, 210.5")
    assertNull(invalidLng)
  }

  @Test
  fun coordinateParser_rejectsTextQueries() {
    val textQuery = CoordinateParser.parse("Walgreens Pharmacy Market St")
    assertNull(textQuery)
  }
}
