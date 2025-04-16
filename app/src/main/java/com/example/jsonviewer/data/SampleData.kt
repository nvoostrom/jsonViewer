package com.example.jsonviewer.data

/**
 * This class provides sample JSON data for demonstration purposes
 */
object SampleData {
    // Original sample JSON with conflict data
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

    // Simple user profile sample
    val userProfileJson = """
{
  "user": {
    "id": "u12345",
    "username": "jsonexplorer",
    "email": "user@example.com",
    "profile": {
      "firstName": "Alex",
      "lastName": "Morgan",
      "avatar": "https://example.com/avatar.jpg",
      "bio": "I love exploring JSON data structures!",
      "location": {
        "city": "San Francisco",
        "country": "USA",
        "timezone": "PST"
      }
    },
    "preferences": {
      "theme": "dark",
      "notifications": true,
      "privacy": {
        "showEmail": false,
        "showLocation": true
      }
    },
    "stats": {
      "joined": "2023-06-15",
      "lastActive": "2025-04-15",
      "postsCount": 42,
      "followersCount": 156,
      "followingCount": 97
    },
    "roles": ["user", "moderator"],
    "verified": true
  }
}
    """.trimIndent()

    // Weather forecast sample
    val weatherJson = """
{
  "location": {
    "name": "New York",
    "region": "New York",
    "country": "United States",
    "lat": 40.71,
    "lon": -74.01,
    "timezone": "America/New_York"
  },
  "current": {
    "temp_c": 18.3,
    "temp_f": 64.9,
    "condition": {
      "text": "Partly cloudy",
      "icon": "//cdn.weatherapi.com/weather/64x64/day/116.png",
      "code": 1003
    },
    "wind_mph": 6.9,
    "wind_kph": 11.2,
    "wind_dir": "ENE",
    "humidity": 63,
    "feelslike_c": 18.3,
    "feelslike_f": 64.9,
    "uv": 5.0
  },
  "forecast": {
    "forecastday": [
      {
        "date": "2025-04-16",
        "date_epoch": 1718553600,
        "day": {
          "maxtemp_c": 22.6,
          "maxtemp_f": 72.7,
          "mintemp_c": 13.9,
          "mintemp_f": 57.0,
          "condition": {
            "text": "Sunny",
            "icon": "//cdn.weatherapi.com/weather/64x64/day/113.png",
            "code": 1000
          },
          "uv": 5.0
        },
        "astro": {
          "sunrise": "06:14 AM",
          "sunset": "07:36 PM",
          "moonrise": "11:20 AM",
          "moonset": "02:29 AM",
          "moon_phase": "Waxing Gibbous",
          "moon_illumination": "60"
        },
        "hour": [
          {
            "time_epoch": 1718535600,
            "time": "2025-04-16 00:00",
            "temp_c": 16.1,
            "temp_f": 61.0,
            "condition": {
              "text": "Clear",
              "icon": "//cdn.weatherapi.com/weather/64x64/night/113.png",
              "code": 1000
            },
            "wind_mph": 5.6,
            "wind_dir": "ENE",
            "humidity": 72,
            "feelslike_c": 16.1,
            "feelslike_f": 61.0
          }
        ]
      }
    ]
  }
}
    """.trimIndent()

    // Product catalog sample
    val productJson = """
{
  "catalog": {
    "name": "Summer Collection 2025",
    "lastUpdated": "2025-03-15",
    "categories": [
      {
        "id": "cat-001",
        "name": "Clothing",
        "products": [
          {
            "id": "prod-101",
            "name": "Casual T-Shirt",
            "description": "Comfortable cotton t-shirt for everyday wear",
            "price": 24.99,
            "currency": "USD",
            "variants": [
              {
                "size": "S",
                "color": "Blue",
                "inStock": true,
                "sku": "TS-BL-S"
              },
              {
                "size": "M",
                "color": "Blue",
                "inStock": true,
                "sku": "TS-BL-M"
              },
              {
                "size": "L",
                "color": "Blue",
                "inStock": false,
                "sku": "TS-BL-L"
              }
            ],
            "images": [
              "https://example.com/images/ts-blue-front.jpg",
              "https://example.com/images/ts-blue-back.jpg"
            ],
            "tags": ["casual", "cotton", "summer"],
            "ratings": {
              "average": 4.5,
              "count": 128
            }
          }
        ]
      }
    ]
  }
}
    """.trimIndent()
}