package com.example.core.model

import androidx.annotation.DrawableRes
import com.example.R

enum class TourismCategory(val label: String) {
    ALL("All Spots"),
    BEACHES("Beaches"),
    ECO("Mountains & Eco"),
    HERITAGE("Heritage & Crafts"),
    FOOD("Food & Culture")
}

data class SanJuanDestination(
    val id: String,
    val name: String,
    val barangay: String,
    val category: TourismCategory,
    val tagline: String,
    val description: String,
    val bestTime: String,
    val estimatedBudget: String,
    val highlights: List<String>,
    val travelTip: String,
    @param:DrawableRes val imageRes: Int
)

object SanJuanCatalog {
    val destinations: List<SanJuanDestination> = listOf(
        SanJuanDestination(
            id = "laiya_beach",
            name = "Laiya White Beach & Resorts",
            barangay = "Brgy. Laiya Aplaya & Laiya Ibabao",
            category = TourismCategory.BEACHES,
            tagline = "Powdery cream sand & calm Tayabas Bay waters",
            description = "San Juan's flagship 6-kilometer coastline of crushed-coral cream sand, crystal turquoise coves, and family-friendly beach resorts facing Tayabas Bay.",
            bestTime = "6:00 AM – 10:30 AM & Sunset",
            estimatedBudget = "PHP 300 – 1,500 day tour",
            highlights = listOf("Kayaking & Paddleboarding", "Sunrise Shoreline Walk", "Bangka Island Hopping"),
            travelTip = "Arrive early on weekends; public beach access points and resort day-pass cottages open around 6:00 AM.",
            imageRes = R.drawable.img_laiya_hero
        ),
        SanJuanDestination(
            id = "hugom_marine",
            name = "Hugom Coral Cove & Marine Sanctuary",
            barangay = "Brgy. Hugom",
            category = TourismCategory.BEACHES,
            tagline = "Protected snorkeling reefs & quiet coastal coves",
            description = "Nestled past Laiya at the foot of Mt. Daguldol, Hugom offers clear reef waters ideal for snorkeling, freediving practice, and peaceful pebble-and-sand coves.",
            bestTime = "7:00 AM – 11:00 AM (Low Wind)",
            estimatedBudget = "PHP 250 – 600 snorkel rental",
            highlights = listOf("Coral Garden Snorkeling", "Boat Jump-off Cove", "Eco-Tourism Fees"),
            travelTip = "Use reef-safe sunscreen and register at the Brgy. Hugom Tourism Desk before entering the sanctuary.",
            imageRes = R.drawable.img_laiya_hero
        ),
        SanJuanDestination(
            id = "mt_daguldol",
            name = "Mount Daguldol Coastal Trail",
            barangay = "Brgy. Hugom",
            category = TourismCategory.ECO,
            tagline = "Beginner-friendly coastal summit above Laiya",
            description = "A scenic 672-MASL coastal peak offering panoramic views of Tayabas Bay, Marinduque on clear mornings, shaded forest trails, and iconic trail-side buko stops.",
            bestTime = "5:00 AM Start (Sunrise Ascent)",
            estimatedBudget = "PHP 500 – 800 guide & registration",
            highlights = listOf("Tayabas Bay Ridge View", "Niyugan Campsite", "Mandatory Local Guide"),
            travelTip = "Register at the Brgy. Hugom Barangay Hall; accredited local guides lead the 2.5-hour ascent.",
            imageRes = R.drawable.img_mt_daguldol
        ),
        SanJuanDestination(
            id = "subukin_mangrove",
            name = "Subukin Mangrove Boardwalk & Eco-Park",
            barangay = "Brgy. Subukin",
            category = TourismCategory.ECO,
            tagline = "Coastal mangrove sanctuary & bamboo walkways",
            description = "A peaceful community-managed mangrove reserve featuring elevated bamboo paths, bird-watching stations, and educational coastal conservation tours.",
            bestTime = "7:30 AM – 9:30 AM or 4:00 PM",
            estimatedBudget = "PHP 100 – 250 environmental fee",
            highlights = listOf("Mangrove Reforestation", "Coastal Birding", "Sunset Boardwalk"),
            travelTip = "Pair this stop with an afternoon drive to Poblacion for warm Batangas lomi.",
            imageRes = R.drawable.img_mt_daguldol
        ),
        SanJuanDestination(
            id = "nepomuceno_parish",
            name = "San Juan Nepomuceno Parish & Heritage Plaza",
            barangay = "Brgy. Poblacion",
            category = TourismCategory.HERITAGE,
            tagline = "19th-century stone parish & ancestral town center",
            description = "Built in the late 1800s after San Juan moved inland, the historic church and surrounding Poblacion ancestral houses showcase classic Batangueño colonial architecture.",
            bestTime = "8:00 AM – 5:00 PM",
            estimatedBudget = "Free admission",
            highlights = listOf("Historic Facade & Belfry", "Town Plaza Monument", "Ancestral Houses Walk"),
            travelTip = "Visit around 4:30 PM when golden hour illuminates the plaza and local street vendors open nearby.",
            imageRes = R.drawable.img_sanjuan_heritage
        ),
        SanJuanDestination(
            id = "pinagbayanan_pottery",
            name = "Pinagbayanan Traditional Pottery Village",
            barangay = "Brgy. Pinagbayanan",
            category = TourismCategory.HERITAGE,
            tagline = "Generational red-clay palayok & banga craftsmanship",
            description = "Discover San Juan's famous living craft tradition where artisan families hand-mold terracotta cooking pots, kalan stoves, and decorative planters using river clay.",
            bestTime = "9:00 AM – 4:00 PM",
            estimatedBudget = "PHP 150 – 500 souvenir clayware",
            highlights = listOf("Live Clay Shaping", "Kiln Firing Tradition", "Artisan Souvenir Stalls"),
            travelTip = "Ask local potters for a quick wheel-throwing demo and pack clay pieces carefully for travel.",
            imageRes = R.drawable.img_sanjuan_heritage
        ),
        SanJuanDestination(
            id = "barako_lambanog_hub",
            name = "Kapeng Barako & Lambanog Tasting Trail",
            barangay = "Brgy. Buhay na Sapa & Poblacion",
            category = TourismCategory.FOOD,
            tagline = "Bold Batangas liberica coffee & coconut craft spirits",
            description = "San Juan is celebrated for traditional lambanog coconut distilleries and freshly roasted Kapeng Barako beans served alongside crisp tablea chocolate and bibingka.",
            bestTime = "8:00 AM Coffee / 3:00 PM Tasting",
            estimatedBudget = "PHP 150 – 450 tastings & pasalubong",
            highlights = listOf("Liberica Barako Brew", "Artisanal Coconut Lambanog", "Pasalubong Roasteries"),
            travelTip = "Pick up whole-bean Barako and spiced lambanog bottles directly from barangay producers before heading home.",
            imageRes = R.drawable.img_batangas_cuisine
        ),
        SanJuanDestination(
            id = "poblacion_lomi_stalls",
            name = "San Juan Lomi & Kakanin Food Walk",
            barangay = "San Juan Public Market & Poblacion",
            category = TourismCategory.FOOD,
            tagline = "Rich Batangas lomi, chicharon & suman sa lihiya",
            description = "Refuel after beach or mountain adventures with authentic thick-noodle Batangas lomi topped with kikiam, pork liver, and crispy chicharon at bustling town eateries.",
            bestTime = "11:30 AM – 2:00 PM & 5:30 PM",
            estimatedBudget = "PHP 120 – 300 per meal",
            highlights = listOf("Special Batangas Lomi", "Suman & Kalamay", "Market Pasalubong"),
            travelTip = "Most classic lomi houses around Poblacion accept cash only, so bring small bills.",
            imageRes = R.drawable.img_batangas_cuisine
        )
    )
}
