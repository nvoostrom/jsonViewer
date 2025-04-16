package com.example.jsonviewer.data

/**
 * This class provides sample JSON data for demonstration purposes
 */
object SampleData {
    val sampleJson = """
{
  "notFound": [
    {
      "title": "Nukita L - I live on an island straight from a fap game, what on earth should I do?",
      "status": "not found",
      "details": "<svg data-v-583c6f80=\"\" xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke-width=\"2\" stroke=\"currentColor\" aria-hidden=\"true\" class=\"icon\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z\"></path></svg><p data-v-583c6f80=\"\" class=\"mt-4 font-semibold text-gray-900 dark_text-white\">No results found</p><p data-v-583c6f80=\"\" class=\"mt-2 text-gray-500 dark_text-gray-400\">No series found for this search term.<br>Try an alternative title or paste a URL to suggest a new series</p>"
    }
  ],
  "singleResult": [],
  "multipleConflicts": [
    {
      "title": "Villain To Kill",
      "status": "multiple results with conflict",
      "results": [
        {
          "headline": "Villain to Kill",
          "score": 1,
          "alreadyInList": true
        },
        {
          "headline": "The Great Villain Who Threatened to Kill Himself",
          "score": 1,
          "alreadyInList": false
        }
      ]
    },
    {
      "title": "Hero Has Returned",
      "status": "multiple results with conflict",
      "results": [
        {
          "headline": "The Max Level Hero Has Returned!",
          "score": 1,
          "alreadyInList": true
        },
        {
          "headline": "A Disaster Class Hero Has Returned",
          "score": 1,
          "alreadyInList": false
        },
        {
          "headline": "Hero Has Returned",
          "score": 1,
          "alreadyInList": true
        }
      ]
    },
    {
      "title": "All Rounder",
      "status": "multiple results with conflict",
      "results": [
        {
          "headline": "All Rounder",
          "score": 1,
          "alreadyInList": true
        },
        {
          "headline": "All-Rounder Meguru",
          "score": 0,
          "alreadyInList": false
        },
        {
          "headline": "The Second Life of an All-Rounder Idol",
          "score": 0,
          "alreadyInList": false
        },
        {
          "headline": "Become a Legend as an All-Rounder",
          "score": 0,
          "alreadyInList": false
        },
        {
          "headline": "The Ultimate All-rounder",
          "score": 0,
          "alreadyInList": false
        },
        {
          "headline": "The Ultimate All-Rounder Remake",
          "score": 0,
          "alreadyInList": false
        },
        {
          "headline": "Starting a New Life for the Discarded All-Rounder",
          "score": 0,
          "alreadyInList": true
        }
      ]
    }
  ]
}
    """.trimIndent()
}