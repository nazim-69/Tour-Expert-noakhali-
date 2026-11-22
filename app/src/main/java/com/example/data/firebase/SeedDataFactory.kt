package com.example.data.firebase

import com.example.data.model.AdminAccount
import com.example.data.model.AgencySettings
import com.example.data.model.BrandConstants
import com.example.data.model.DestinationPlace
import com.example.data.model.TeamMember
import com.example.data.model.TourEvent
import com.example.data.security.PasswordHasher

object SeedDataFactory {

    val defaultSettings = AgencySettings(
        id = "agency",
        organizationName = BrandConstants.ORGANIZATION_NAME,
        tagline = BrandConstants.TAGLINE,
        address = BrandConstants.ADDRESS,
        contactPhone = BrandConstants.CONTACT_PHONE,
        bookingPhone = BrandConstants.BOOKING_PHONE,
        advancePaymentNumber = BrandConstants.ADVANCE_PAYMENT_NUMBER,
        facebookUrl = null,
        whatsappUrl = "https://wa.me/8801822421919",
        websiteUrl = BrandConstants.DEFAULT_WEB_APP_URL,
        welcomeBannerText = "আসসালামুয়ালাইকুম। ট্যুর এক্সপার্ট নোয়াখালী আয়োজিত আমাদের অক্টোবর মাসের ইভেন্টসমূহে বুকিং চলছে!",
        activeMonthTitle = "অক্টোবর মাসের ইভেন্টসমূহ",
        bookingCounter = 0
    )

    val defaultEvents = listOf(
        TourEvent(
            id = "event_sundarbans_oct08",
            name = "০৮ অক্টোবর সুন্দরবন ভ্রমণ",
            destination = "সুন্দরবন",
            tourDate = "০৮ অক্টোবর",
            monthTag = "THIS_MONTH",
            category = "SEA",
            duration = "২ দিন ১ রাত",
            price = 2199,
            advancePayment = 1000,
            shortDescription = "সুন্দরবনের করমজল, হারবাড়িয়া ও ম্যানগ্রোভ বনাঞ্চলে নৌ-ভ্রমণ — মাত্র ২১৯৯ টাকায়।",
            description = "নোয়াখালী থেকে সরাসরি আরামদায়ক ট্যুরিস্ট বাসে মংলা/খুলনা ও পরে ক্রুজ বোটে সুন্দরবন ইকো-ট্যুর।\n\nপ্যাকেজে অন্তর্ভুক্ত:\n• নোয়াখালী থেকে যাতায়াত (আসা-যাওয়া)\n• সারাদিন ম্যানগ্রোভ ক্রুজ ও গাইড সার্ভিস\n• ৩ বেলা মানসম্মত খাবার ও নাস্তা\n• ফরেস্ট এন্ট্রি পাস ও নিরাপত্তা ব্যবস্থা",
            imageKey = "sundarbans",
            galleryKeys = listOf("sundarbans", "hero_bangladesh"),
            totalSeats = 40,
            bookedSeats = emptyList(),
            holdSeats = emptyList(),
            vehicleLayout = "COACH_40",
            bookingStatus = "OPEN",
            featured = true
        ),
        TourEvent(
            id = "event_rangamati_oct08",
            name = "০৮ অক্টোবর রাঙামাটি ডে ট্যুর",
            destination = "রাঙামাটি",
            tourDate = "০৮ অক্টোবর",
            monthTag = "THIS_MONTH",
            category = "BUDGET",
            duration = "১ রাত ১ দিন",
            price = 1399,
            advancePayment = 700,
            shortDescription = "কাপ্তাই লেক, ঝুলন্ত ব্রিজ ও শুভলং ঝরনা ভ্রমণ — মাত্র ১৩৯৯ টাকায়।",
            description = "বাজেটবান্ধব ও ফ্যামিলি-ফ্রেন্ডলি রাঙামাটি ভ্রমণ প্যাকেজ।\n\nপ্যাকেজে অন্তর্ভুক্ত:\n• নোয়াখালী থেকে রিজার্ভড বাসে যাতায়াত\n• কাপ্তাই লেকে রিজার্ভ বোটে সারাদিন ভ্রমণ\n• ঝুলন্ত ব্রিজ, পলওয়েল পার্ক ও শুভলং ঝরনা\n• সকালের নাস্তা ও দুপুরের পাহাড়ি খাবার",
            imageKey = "hill_tracts",
            galleryKeys = listOf("hill_tracts", "hero_bangladesh"),
            totalSeats = 40,
            bookedSeats = emptyList(),
            holdSeats = emptyList(),
            vehicleLayout = "COACH_40",
            bookingStatus = "OPEN",
            featured = true
        ),
        TourEvent(
            id = "event_bandarban_oct15",
            name = "১৫ অক্টোবর বান্দরবান ভ্রমণ",
            destination = "বান্দরবান",
            tourDate = "১৫ অক্টোবর",
            monthTag = "THIS_MONTH",
            category = "HILL",
            duration = "২ রাত ১ দিন",
            price = 2199,
            advancePayment = 1000,
            shortDescription = "নীলগিরি, নীলাচল, চিম্বুক পাহাড় ও মেঘলা পর্যটন কেন্দ্র — মাত্র ২১৯৯ টাকায়।",
            description = "মেঘের রাজ্য বান্দরবানে রোমাঞ্চকর পাহাড়ি ভ্রমণ।\n\nপ্যাকেজে অন্তর্ভুক্ত:\n• নোয়াখালী থেকে সরাসরি বাস যাতায়াত\n• স্থানীয় চান্দের গাড়িতে নীলগিরি ও নীলাচল সাইটসিইং\n• সকালের নাস্তা, দুপুরের খাবার ও সন্ধ্যার স্ন্যাকস\n• অভিজ্ঞ ট্যুর গাইড ও ফটোগ্রাফি সহায়তা",
            imageKey = "hero_bangladesh",
            galleryKeys = listOf("hero_bangladesh", "hill_tracts"),
            totalSeats = 28,
            bookedSeats = emptyList(),
            holdSeats = emptyList(),
            vehicleLayout = "MINIBUS_28",
            bookingStatus = "OPEN",
            featured = true
        ),
        TourEvent(
            id = "event_sreemangal_oct15",
            name = "১৫ অক্টোবর শ্রীমঙ্গল চা-বাগান ট্যুর",
            destination = "শ্রীমঙ্গল",
            tourDate = "১৫ অক্টোবর",
            monthTag = "THIS_MONTH",
            category = "FAMILY",
            duration = "২ রাত ১ দিন",
            price = 1899,
            advancePayment = 900,
            shortDescription = "সবুজ চা-বাগান, লাউয়াছড়া উদ্যান, মাধবপুর লেক ও বাইক্কা বিল — মাত্র ১৮৯৯ টাকা।",
            description = "প্রকৃতির সবুজ গালিচা শ্রীমঙ্গলে প্রশান্তিদায়ক ফ্যামিলি ও শিক্ষা সফর উপযোগী ট্যুর।\n\nপ্যাকেজে অন্তর্ভুক্ত:\n• নোয়াখালী থেকে রিজার্ভ বাসে যাতায়াত\n• লাউয়াছড়া জাতীয় উদ্যান ও মাধবপুর লেক ভ্রমণ\n• ৭ রঙের চা অভিজ্ঞতা ও চা-বাগান পরিদর্শন\n• সকাল ও দুপুরের মানসম্মত খাবার",
            imageKey = "sylhet_coast",
            galleryKeys = listOf("sylhet_coast", "hero_bangladesh"),
            totalSeats = 40,
            bookedSeats = emptyList(),
            holdSeats = emptyList(),
            vehicleLayout = "COACH_40",
            bookingStatus = "OPEN",
            featured = true
        )
    )

    val defaultDestinations = listOf(
        DestinationPlace(
            id = "dest_sajek",
            name = "সাজেক",
            division = "রাঙামাটি, চট্টগ্রাম",
            shortDescription = "মেঘের উপত্যকা খ্যাত বাংলাদেশের সবচেয়ে জনপ্রিয় পাহাড়ি পর্যটন কেন্দ্র।",
            fullDetails = "সমুদ্রপৃষ্ঠ থেকে প্রায় ১৮০০ ফুট উচ্চতায় অবস্থিত সাজেক ভ্যালি। রুইলুই পাড়া, কংলাক পাহাড় আর ভোরের সূর্যোদয় ও মেঘের সমুদ্র দেখতে প্রতি বছর হাজারো ভ্রমণপিপাসু ছুটে আসেন।",
            bestTime = "অক্টোবর – মার্চ",
            imageKey = "hero_bangladesh",
            relatedEventIds = listOf("event_bandarban_oct15"),
            sortOrder = 1
        ),
        DestinationPlace(
            id = "dest_coxs_bazar",
            name = "কক্সবাজার",
            division = "চট্টগ্রাম",
            shortDescription = "বিশ্বের দীর্ঘতম অখণ্ড প্রাকৃতিক সমুদ্র সৈকত, ইনানী ও মেরিন ড্রাইভ।",
            fullDetails = "১২০ কিলোমিটার দীর্ঘ সোনালি বালুকাময় সৈকত, হিমছড়ি ঝরনা, ইনানী প্রবাল পাথর এবং মনোরম মেরিন ড্রাইভ রোড ভ্রমণকারীদের কাছে চিরসবুজ আকর্ষণ।",
            bestTime = "নভেম্বর – মার্চ",
            imageKey = "sundarbans",
            relatedEventIds = listOf("event_sundarbans_oct08"),
            sortOrder = 2
        ),
        DestinationPlace(
            id = "dest_sundarbans",
            name = "সুন্দরবন",
            division = "খুলনা",
            shortDescription = "বিশ্বের বৃহত্তম ম্যানগ্রোভ বন, রয়েল বেঙ্গল টাইগারের আবাসস্থল ও নদীনালা।",
            fullDetails = "ইউনেস্কো বিশ্ব ঐতিহ্যবাহী সুন্দরবন প্রকৃতির এক অনন্য বিস্ময়। করমজল, হারবাড়িয়া, কটকা ও জামতলা সৈকতে নৌ-ভ্রমণ আপনাকে প্রকৃতির কাছাকাছি নিয়ে যাবে।",
            bestTime = "অক্টোবর – ফেব্রুয়ারি",
            imageKey = "sundarbans",
            relatedEventIds = listOf("event_sundarbans_oct08"),
            sortOrder = 3
        ),
        DestinationPlace(
            id = "dest_rangamati",
            name = "রাঙামাটি",
            division = "চট্টগ্রাম",
            shortDescription = "কাপ্তাই লেকের নীল জলরাশি, ঝুলন্ত সেতু ও আদিবাসী সংস্কৃতির অপূর্ব মিলন।",
            fullDetails = "স্বচ্ছ নীল কাপ্তাই লেক, শুভলং ঝরনা, ঝুলন্ত ব্রিজ এবং সবুজ পাহাড়ের কোলে গড়ে ওঠা রাঙামাটি এক দিনের রিফ্রেশিং ভ্রমণের জন্য সেরা।",
            bestTime = "সারা বছর (বিশেষত শরৎ-শীত)",
            imageKey = "hill_tracts",
            relatedEventIds = listOf("event_rangamati_oct08"),
            sortOrder = 4
        ),
        DestinationPlace(
            id = "dest_bandarban",
            name = "বান্দরবান",
            division = "চট্টগ্রাম",
            shortDescription = "নীলগিরি, নীলাচল, বগালেক ও মেঘছোঁয়া পর্বতশৃঙ্গের রোমাঞ্চকর গন্তব্য।",
            fullDetails = "বাংলাদেশের সর্বোচ্চ পর্বতশৃঙ্গগুলোর আবাসস্থল বান্দরবান। নীলগিরি, নীলাচল, চিম্বুক, শৈলপ্রপাত ও মেঘলা পর্যটন কমপ্লেক্স ভ্রমণে এনে দেয় অনন্য রোমাঞ্চ।",
            bestTime = "অক্টোবর – এপ্রিল",
            imageKey = "hero_bangladesh",
            relatedEventIds = listOf("event_bandarban_oct15"),
            sortOrder = 5
        ),
        DestinationPlace(
            id = "dest_sylhet",
            name = "সিলেট",
            division = "সিলেট",
            shortDescription = "সাদা পাথর, জাফলং, রাতারগুল সোয়াম্প ফরেস্ট ও বিছানাকান্দির সৌন্দর্য।",
            fullDetails = "পাহাড়, স্বচ্ছ নদী আর জলারবনের অপূর্ব মেলবন্ধন সিলেট। ভোলাগঞ্জ সাদা পাথর, রাতারগুল জলারবন এবং জাফলং ভ্রমণকারীদের কাছে অনন্য অভিজ্ঞতা।",
            bestTime = "বর্ষা ও শীতকাল",
            imageKey = "sylhet_coast",
            relatedEventIds = listOf("event_sreemangal_oct15"),
            sortOrder = 6
        ),
        DestinationPlace(
            id = "dest_sreemangal",
            name = "শ্রীমঙ্গল",
            division = "মৌলভীবাজার, সিলেট",
            shortDescription = "চায়ের রাজধানী — দিগন্তজোড়া সবুজ চা-বাগান, লাউয়াছড়া ও মাধবপুর লেক।",
            fullDetails = "নৈসর্গিক চা-বাগান, লাউয়াছড়া রেইনফরেস্ট, বাইক্কা বিল পাখির অভয়াশ্রম ও নীলকণ্ঠের সাত রঙের চা শ্রীমঙ্গলকে করেছে ভ্রমণপিপাসুদের প্রিয় গন্তব্য।",
            bestTime = "অক্টোবর – মার্চ",
            imageKey = "sylhet_coast",
            relatedEventIds = listOf("event_sreemangal_oct15"),
            sortOrder = 7
        ),
        DestinationPlace(
            id = "dest_tanguar_haor",
            name = "টাঙ্গুয়ার হাওর",
            division = "সুনামগঞ্জ, সিলেট",
            shortDescription = "হাউসবোট, নীলাদ্রি লেক, বারিক্কা টিলা ও দিগন্তবিস্তৃত জলরাশি।",
            fullDetails = "মেঘালয় পাহাড়ের পাদদেশে বিস্তৃত টাঙ্গুয়ার হাওরে হাউসবোটে রাত্রিযাপন, শহীদ সিরাজ লেক (নীলাদ্রি) ও যাদুকাটা নদীর সৌন্দর্য ভোলার নয়।",
            bestTime = "জুন – অক্টোবর",
            imageKey = "hill_tracts",
            relatedEventIds = listOf("event_sreemangal_oct15"),
            sortOrder = 8
        )
    )

    val defaultTeamMembers: List<TeamMember> = BrandConstants.OFFICIAL_ADMIN_NAMES.mapIndexed { idx, name ->
        val num = idx + 1
        TeamMember(
            id = "admin_$num",
            name = name,
            designation = if (num == 8) "সুপার এডমিন ও সমন্বয়ক" else "ট্যুর এডমিন ও গাইড",
            sortOrder = num,
            showPublicContact = false,
            publicPhone = null,
            whatsappUrl = null,
            facebookUrl = null
        )
    }

    fun buildInitialAdminAccounts(): List<AdminAccount> {
        val usernames = listOf(
            "abdulhakim",
            "masudparvez",
            "omarfaruk",
            "mustakimadil",
            "shamsulopi",
            "hasanmahmud",
            "abdurrahim",
            "nazimuddin"
        )
        return BrandConstants.OFFICIAL_ADMIN_NAMES.mapIndexed { idx, name ->
            val num = idx + 1
            val isSuper = num == 8
            val salt = PasswordHasher.generateSaltHex()
            // Initial setup placeholder hash (disabled for password login until Super Admin sets passcode,
            // or Super Admin uses Google Sign-In directly and sets individual passcodes in Dashboard).
            val placeholderSecret = PasswordHasher.generateSaltHex()
            val hash = PasswordHasher.hashPassword(placeholderSecret, salt)
            AdminAccount(
                id = "admin_$num",
                name = name,
                username = usernames[idx],
                designation = if (isSuper) "সুপার এডমিন" else "এডমিন",
                role = if (isSuper) "SUPER_ADMIN" else "ADMIN",
                passwordHash = hash,
                passwordSalt = salt,
                boundEmail = if (isSuper) BrandConstants.SUPER_ADMIN_EMAIL else null,
                phone = null,
                whatsappUrl = null,
                facebookUrl = null,
                canManageEvents = isSuper,
                canVerifyPayments = isSuper,
                canManageBookings = true,
                canManageDestinations = isSuper,
                canManageSettings = isSuper,
                active = true,
                sortOrder = num
            )
        }
    }
}
