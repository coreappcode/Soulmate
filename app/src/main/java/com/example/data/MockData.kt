package com.example.data

object MockData {
    val popularCountries = listOf(
        Country("India", "🇮🇳", "+91"),
        Country("USA", "🇺🇸", "+1"),
        Country("UK", "🇬🇧", "+44"),
        Country("Canada", "🇨🇦", "+1"),
        Country("Australia", "🇦🇺", "+61")
    )

    val allCountries = listOf(
        Country("Argentina", "🇦🇷", "+54"),
        Country("Australia", "🇦🇺", "+61"),
        Country("Austria", "🇦🇹", "+43"),
        Country("Belgium", "🇧🇪", "+32"),
        Country("Brazil", "🇧🇷", "+55"),
        Country("Canada", "🇨🇦", "+1"),
        Country("Denmark", "🇩🇰", "+45"),
        Country("Finland", "🇫🇮", "+358"),
        Country("France", "🇫🇷", "+33"),
        Country("Germany", "🇩🇪", "+49"),
        Country("India", "🇮🇳", "+91"),
        Country("Indonesia", "🇮🇩", "+62"),
        Country("Ireland", "🇮🇪", "+353"),
        Country("Italy", "🇮🇹", "+39"),
        Country("Japan", "🇯🇵", "+81"),
        Country("Mexico", "🇲🇽", "+52"),
        Country("Netherlands", "🇳🇱", "+31"),
        Country("New Zealand", "🇳🇿", "+64"),
        Country("Norway", "🇳🇴", "+47"),
        Country("Portugal", "🇵🇹", "+351"),
        Country("Singapore", "🇸🇬", "+65"),
        Country("South Korea", "🇰🇷", "+82"),
        Country("Spain", "🇪🇸", "+34"),
        Country("Sweden", "🇸🇪", "+46"),
        Country("Switzerland", "🇨🇭", "+41"),
        Country("UK", "🇬🇧", "+44"),
        Country("USA", "🇺🇸", "+1"),
        Country("Vietnam", "🇻🇳", "+84")
    )

    val users: List<User> = listOf(
        User(
            id = "user_1",
            name = "Aarav Sharma",
            username = "aarav_s",
            age = 27,
            gender = "Man",
            bio = "Architect by day, vinyl collector by night. Looking for someone who enjoys rooftop coffees and sunset walks.",
            photos = listOf(
                "https://picsum.photos/seed/user1a/400/600",
                "https://picsum.photos/seed/user1b/400/600",
                "https://picsum.photos/seed/user1c/400/600"
            ),
            distance = "2 km",
            country = "India",
            verified = true,
            interests = listOf("Music", "Coffee", "Architecture", "Photography"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_2",
            name = "Chloe Dubois",
            username = "chloe_paris",
            age = 24,
            gender = "Woman",
            bio = "Pastry chef exploring new cities. Tell me your favorite indie film and bakery spot!",
            photos = listOf(
                "https://picsum.photos/seed/user2a/400/600",
                "https://picsum.photos/seed/user2b/400/600",
                "https://picsum.photos/seed/user2c/400/600"
            ),
            distance = "4 km",
            country = "Canada",
            verified = true,
            interests = listOf("Cooking", "Movies", "Travel", "Art"),
            lookingFor = listOf("Men", "Women")
        ),
        User(
            id = "user_3",
            name = "Rohan Mehta",
            username = "rohan_m",
            age = 31,
            gender = "Man",
            bio = "Software engineer who loves weekend trail running and indie rock concerts.",
            photos = listOf(
                "https://picsum.photos/seed/user3a/400/600",
                "https://picsum.photos/seed/user3b/400/600",
                "https://picsum.photos/seed/user3c/400/600"
            ),
            distance = "5 km",
            country = "India",
            verified = true,
            interests = listOf("Tech", "Fitness", "Music", "Nature"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_4",
            name = "Hana Tanaka",
            username = "hana_sky",
            age = 26,
            gender = "Woman",
            bio = "Ceramics artist and green tea connoisseur. Constantly hunting for cozy bookstore nooks.",
            photos = listOf(
                "https://picsum.photos/seed/user4a/400/600",
                "https://picsum.photos/seed/user4b/400/600",
                "https://picsum.photos/seed/user4c/400/600"
            ),
            distance = "3 km",
            country = "Japan",
            verified = true,
            interests = listOf("Art", "Reading", "Coffee", "Yoga"),
            lookingFor = listOf("Men")
        ),
        User(
            id = "user_5",
            name = "Alex Rivera",
            username = "alex_vibes",
            age = 29,
            gender = "Non-binary",
            bio = "DJ and plant enthusiast. Creating thoughtful playlists for long road trips.",
            photos = listOf(
                "https://picsum.photos/seed/user5a/400/600",
                "https://picsum.photos/seed/user5b/400/600",
                "https://picsum.photos/seed/user5c/400/600"
            ),
            distance = "7 km",
            country = "USA",
            verified = true,
            interests = listOf("Music", "Dancing", "Pets", "Tech"),
            lookingFor = listOf("All")
        ),
        User(
            id = "user_6",
            name = "Priya Nair",
            username = "priya_adventures",
            age = 25,
            gender = "Woman",
            bio = "Marine biologist who can never spend enough time by the ocean. Let's go scuba diving!",
            photos = listOf(
                "https://picsum.photos/seed/user6a/400/600",
                "https://picsum.photos/seed/user6b/400/600",
                "https://picsum.photos/seed/user6c/400/600"
            ),
            distance = "8 km",
            country = "India",
            verified = true,
            interests = listOf("Travel", "Nature", "Photography", "Fitness"),
            lookingFor = listOf("Men")
        ),
        User(
            id = "user_7",
            name = "Liam O'Connor",
            username = "liam_dublin",
            age = 33,
            gender = "Man",
            bio = "Writer and part-time guitarist. Always up for museum dates and good craft beer.",
            photos = listOf(
                "https://picsum.photos/seed/user7a/400/600",
                "https://picsum.photos/seed/user7b/400/600",
                "https://picsum.photos/seed/user7c/400/600"
            ),
            distance = "12 km",
            country = "UK",
            verified = false,
            interests = listOf("Reading", "Music", "Wine", "Travel"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_8",
            name = "Ananya Sen",
            username = "ananya_s",
            age = 28,
            gender = "Woman",
            bio = "Product designer with a penchant for specialty roasts and sourdough baking.",
            photos = listOf(
                "https://picsum.photos/seed/user8a/400/600",
                "https://picsum.photos/seed/user8b/400/600",
                "https://picsum.photos/seed/user8c/400/600"
            ),
            distance = "6 km",
            country = "India",
            verified = true,
            interests = listOf("Cooking", "Art", "Coffee", "Fashion"),
            lookingFor = listOf("Men")
        ),
        User(
            id = "user_9",
            name = "Lucas Silva",
            username = "lucas_rio",
            age = 30,
            gender = "Man",
            bio = "Passionate about samba, volleyball, and beach bonfires under starlit skies.",
            photos = listOf(
                "https://picsum.photos/seed/user9a/400/600",
                "https://picsum.photos/seed/user9b/400/600",
                "https://picsum.photos/seed/user9c/400/600"
            ),
            distance = "15 km",
            country = "Brazil",
            verified = true,
            interests = listOf("Sports", "Dancing", "Travel", "Food"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_10",
            name = "Maya Lin",
            username = "maya_creates",
            age = 23,
            gender = "Woman",
            bio = "Fashion stylist finding beauty in minimalism. Let's explore vintage flea markets.",
            photos = listOf(
                "https://picsum.photos/seed/user10a/400/600",
                "https://picsum.photos/seed/user10b/400/600",
                "https://picsum.photos/seed/user10c/400/600"
            ),
            distance = "1 km",
            country = "USA",
            verified = true,
            interests = listOf("Fashion", "Art", "Photography", "Music"),
            lookingFor = listOf("Men", "Women")
        ),
        User(
            id = "user_11",
            name = "Kabir Kapoor",
            username = "kabir_k",
            age = 34,
            gender = "Man",
            bio = "Filmmaker and storyteller. Seeking someone who enjoys late night philosophy talks.",
            photos = listOf(
                "https://picsum.photos/seed/user11a/400/600",
                "https://picsum.photos/seed/user11b/400/600",
                "https://picsum.photos/seed/user11c/400/600"
            ),
            distance = "9 km",
            country = "India",
            verified = false,
            interests = listOf("Movies", "Reading", "Coffee", "Art"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_12",
            name = "Elena Rostova",
            username = "elena_dance",
            age = 29,
            gender = "Woman",
            bio = "Classical pianist and yoga instructor. Big fan of hearty brunches and dog parks.",
            photos = listOf(
                "https://picsum.photos/seed/user12a/400/600",
                "https://picsum.photos/seed/user12b/400/600",
                "https://picsum.photos/seed/user12c/400/600"
            ),
            distance = "18 km",
            country = "Canada",
            verified = true,
            interests = listOf("Music", "Yoga", "Pets", "Coffee"),
            lookingFor = listOf("Men")
        ),
        User(
            id = "user_13",
            name = "Samira Al-Hassan",
            username = "samira_wander",
            age = 27,
            gender = "Woman",
            bio = "Documentary photographer. I love street food stalls and catching dawn light.",
            photos = listOf(
                "https://picsum.photos/seed/user13a/400/600",
                "https://picsum.photos/seed/user13b/400/600",
                "https://picsum.photos/seed/user13c/400/600"
            ),
            distance = "11 km",
            country = "UK",
            verified = true,
            interests = listOf("Photography", "Food", "Travel", "Nature"),
            lookingFor = listOf("Men")
        ),
        User(
            id = "user_14",
            name = "Oliver Smith",
            username = "oliver_syd",
            age = 28,
            gender = "Man",
            bio = "Surfer, coffee addict, and amateur astronomer. Show me your coolest constellation.",
            photos = listOf(
                "https://picsum.photos/seed/user14a/400/600",
                "https://picsum.photos/seed/user14b/400/600",
                "https://picsum.photos/seed/user14c/400/600"
            ),
            distance = "22 km",
            country = "Australia",
            verified = true,
            interests = listOf("Sports", "Nature", "Coffee", "Gaming"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_15",
            name = "Jordan Blake",
            username = "jordan_b",
            age = 32,
            gender = "Non-binary",
            bio = "Game developer and board game host. High charisma, slightly clumsy in roller skates.",
            photos = listOf(
                "https://picsum.photos/seed/user15a/400/600",
                "https://picsum.photos/seed/user15b/400/600",
                "https://picsum.photos/seed/user15c/400/600"
            ),
            distance = "14 km",
            country = "USA",
            verified = false,
            interests = listOf("Gaming", "Tech", "Movies", "Food"),
            lookingFor = listOf("All")
        ),
        User(
            id = "user_16",
            name = "Meera Joshi",
            username = "meera_j",
            age = 26,
            gender = "Woman",
            bio = "Kathak dancer and environmental advocate. Seeking genuine conversation and belly laughs.",
            photos = listOf(
                "https://picsum.photos/seed/user16a/400/600",
                "https://picsum.photos/seed/user16b/400/600",
                "https://picsum.photos/seed/user16c/400/600"
            ),
            distance = "16 km",
            country = "India",
            verified = true,
            interests = listOf("Dancing", "Nature", "Reading", "Music"),
            lookingFor = listOf("Men")
        ),
        User(
            id = "user_17",
            name = "Kenji Sato",
            username = "kenji_tokyo",
            age = 35,
            gender = "Man",
            bio = "Restaurateur who makes the best handmade ramen in town. Let's trade favorite recipes.",
            photos = listOf(
                "https://picsum.photos/seed/user17a/400/600",
                "https://picsum.photos/seed/user17b/400/600",
                "https://picsum.photos/seed/user17c/400/600"
            ),
            distance = "25 km",
            country = "Japan",
            verified = true,
            interests = listOf("Cooking", "Food", "Wine", "Travel"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_18",
            name = "Zoe Martin",
            username = "zoe_melb",
            age = 25,
            gender = "Woman",
            bio = "Veterinarian with two golden retrievers. If you have pets, we are already halfway friends.",
            photos = listOf(
                "https://picsum.photos/seed/user18a/400/600",
                "https://picsum.photos/seed/user18b/400/600",
                "https://picsum.photos/seed/user18c/400/600"
            ),
            distance = "30 km",
            country = "Australia",
            verified = true,
            interests = listOf("Pets", "Nature", "Fitness", "Reading"),
            lookingFor = listOf("Men")
        ),
        User(
            id = "user_19",
            name = "Dev Malhotra",
            username = "dev_m",
            age = 30,
            gender = "Man",
            bio = "Fintech founder by weekday, rock climber by weekend. Searching for my belay partner and life co-pilot.",
            photos = listOf(
                "https://picsum.photos/seed/user19a/400/600",
                "https://picsum.photos/seed/user19b/400/600",
                "https://picsum.photos/seed/user19c/400/600"
            ),
            distance = "38 km",
            country = "India",
            verified = true,
            interests = listOf("Fitness", "Tech", "Travel", "Coffee"),
            lookingFor = listOf("Women")
        ),
        User(
            id = "user_20",
            name = "Camila Santos",
            username = "camila_s",
            age = 27,
            gender = "Woman",
            bio = "Botanist fascinated by tropical flora. Let's escape into the forest and watch fireflies.",
            photos = listOf(
                "https://picsum.photos/seed/user20a/400/600",
                "https://picsum.photos/seed/user20b/400/600",
                "https://picsum.photos/seed/user20c/400/600"
            ),
            distance = "45 km",
            country = "Brazil",
            verified = false,
            interests = listOf("Nature", "Art", "Yoga", "Photography"),
            lookingFor = listOf("Men", "Women")
        )
    )
}
