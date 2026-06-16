package com.example.util

object LocalizationHelper {
    private val translations = mapOf(
        "ar" to mapOf(
            "splash_subtitle" to "ذئب التداول",
            "app_title" to "ذئب التداول • WOLF TRADER",
            "app_subtitle" to "نظام الإشارات البرمجية الذكي للمحترفين",
            "activate_license" to "تنشيط رخصة البرنامج",
            "activate_desc" to "أدخل مفتاح التفعيل الخاص بك لتسجيل الدخول والوصول لقائمة صفقات الذئب.",
            "activation_key_label" to "مفتاح التفعيل (Key)",
            "your_device_id" to "معرف الجهاز الخاص بك:",
            "activate_now_btn" to "تفعيل الآن 🐺",
            "no_key" to "ليس لديك مفتاح تفعيل؟",
            "no_key_desc" to "تواصل مع المطور لشراء الترخيص الفوري للنسخة الاحترافية PRO.",
            "buy_key_btn" to "شراء المفتاح عبر تيليجرام 🔗",
            "adv_server_settings" to "إعدادات الخوادم المتقدمة (API Config)",
            "edit_trading_server" to "تعديل خادم التداول النشط:",
            "demo_mode_label" to "وضع المحاكاة الفوري (Demo Mode)",
            "enabled" to "مفعّل",
            "disabled" to "ملغى",
            "demo_hint" to "💡 للتجربة السريعة الفورية دون خادم خارجي: فعّل (وضع المحاكاة) واكتب المفتاح 'WOLF' لتسجيل الدخول مباشرة.",
            
            // Errors
            "err_empty_key" to "⚠️ يرجى إدخال مفتاح التفعيل أولاً",
            "err_invalid_key" to "❌ مفتاح غير صحيح",
            "err_expired_key" to "❌ انتهت صلاحية المفتاح",
            "err_device_mismatch" to "❌ المفتاح مستخدم على جهاز آخر",
            "err_mismatch" to "❌ مفتاح غير صحيح أو غير متطابق",
            "err_connection_failed" to "❌ فشل الاتصال بالخادم. تحقق من تفاصيل الخادم أو استخدم وضع المحاكاة.",

            // Tabs
            "tab_signals" to "الإشارات",
            "tab_markets" to "الأسواق",
            "tab_chart" to "الشارت",
            "tab_settings" to "الإعدادات",

            // Chart Screen
            "chart_title" to "منصة كيوتكس • الشارت الفعلي",
            "chart_desc" to "مؤشرات تحليل حركة السعر اللحظية مع صفقات التداول النشطة وعلامات نقاط الدخول.",
            "chart_no_active_deal" to "لا توجد صفقة نشطة حالية لهذا الزوج",
            "chart_active_deal_buy" to "صفقة صعود (BUY) نشطة",
            "chart_active_deal_sell" to "صفقة هبوط (SELL) نشطة",
            "chart_entry_price" to "سعر الدخول المبرم:",
            "chart_selector_prompt" to "اختر زوج العملات للمعاينة:",
            "chart_live_badge" to "مباشر OTC",

            // HomeDashboard
            "signals_radar" to "رادار الإشارات",
            "demo_badge" to "محاكاة",
            "radar_desc" to "إشارات تحليل الاتجاه الآلي الذكي",
            "active_lvl" to "مستوى التغطية النشطة",
            "active_lvl_desc" to "تم العثور على %d إشارة حاسمة",
            "coverage_desc" to "مبني على خوارزميات تحرك الأسعار بذكاء لتقصي الاتجاه المقاوم.",
            "membership" to "عضوية",
            "strong" to "قوي جداً 💪",
            "medium" to "متوسط ⚡",
            "weak" to "ضعيف 🔍",
            "buy" to "شراء (BUY) 🔥",
            "sell" to "بيع (SELL) ❄️",
            "buy_direction" to "شراء",
            "sell_direction" to "بيع",
            "active_signal_label" to "إشارة نشطة: %s (%d%%)",
            "entry_point" to "نقطة الدخول:",
            "duration" to "مدة الصفقة:",
            "minutes" to "%d دقائق",
            "confidence_rate" to "معدل قوة الثقة:",
            "expired_title" to "انتهت الصلاحية",
            "result_title" to "النتيجة:",
            "win_label" to "ربح ✅",
            "loss_label" to "خسارة ❌",
            "remaining" to "المتبقي:",
            "waiting_signals" to "انتظار تغذية الإشارات...",
            "waiting_desc" to "يقوم خادم تحليل الأسعار حالياً بإنتاج خوارزمية ذكية لاكتشاف الفرص. تأكد من اتصال هاتفك بالإنترنت كلياً أو فعّل وضع المحاكاة بالفور.",
            "risk_warning_title" to "تحذير مخاطر التداول والتحوط",
            "risk_warning_content" to "🚨 التداول يحتوي على مخاطر مرتفعة!\n1. لا تدخل أي صفقة بأكثر من 2% (2/100) من رأس مالك الإجمالي بهدف حماية حسابك وإدارته بذكاء.",

            // MarketsScreen
            "markets_title" to "أسواق الأصول المالية",
            "markets_desc" to "ابحث وصفّ الأصول وقابل إشاراتها الفورية ومعدل ربحيتها.",
            "search_placeholder" to "بحث باسم الزوج (مثال: EUR/USD)",
            "favorites" to "المفضلة",
            "cat_all" to "الكل 🌐",
            "cat_forex" to "فوركس 💱",
            "cat_metals" to "معادن 🪙",
            "cat_crypto" to "كريبتو 🪙",
            "cat_otc" to "سوق موازي OTC 🕰️",
            "no_active_signals" to "لا توجد إشارات نشطة لهذا الزوج حالياً.",
            "no_results_found" to "لم يتم العثور على نتائج",
            "empty_fav_desc" to "المفضلة الخاصة بك فارغة حالياً. اضغط على القلب لتثبيت أزواجك الأكثر تداولاً.",
            "empty_search_desc" to "لم نجد أصول تطابق الاسم '%s'. يرجى إدخال اسم صحيح مثل EUR/USD.",

            // SettingsScreen
            "settings_support_title" to "الإعدادات والدعم 🛠️",
            "account_license_details" to "تفاصيل رخصة الحساب",
            "sub_type" to "نوع الاشتراك المعمّد",
            "sub_pro_value" to "نسخة الذئب اللانهائية (PRO) - %s",
            "device_id_label" to "معرّف الجهاز (Device ID)",
            "feed_health_label" to "سلامة قنوات التغذية",
            "local_sim_active" to "محاكاة محلية نشطة ✅",
            "connected_to_active_server" to "متصل بالخادم النشط 🌐",
            "social_channels" to "قنوات التواصل الاجتماعية للمشتركين",
            "telegram_channel_title" to "قناة صفقات الذئب العامة (قناة تيليجرام)",
            "telegram_channel_desc" to "لمتابعة كواليس السوق وأخبار الصفقات المبرمة مع المشتركين.",
            "telegram_support_title" to "الدعم الفني ومبيعات التفعيل (شراء ترخيص)",
            "telegram_support_desc" to "تواصل مباشرة لشراء الترخيص لفتح التطبيق.",
            "telegram_dev_title" to "تواصل مباشر مع المطور الرئيسي للدعم والمقترحات",
            "telegram_dev_desc" to "لحل المشاكل التقنية أو الاستفسار.",
            "advanced_tools" to "أدوات متقدمة",
            "base_url_label" to "رابط خادم الاتصال (Base URL)",
            "base_url_desc" to "قم بتحديث عنوان URL للاتصال بخادمك الخاص في الإنتاج.",
            "data_sim_title" to "وضع محاكاة البيانات فوري",
            "data_sim_desc" to "توليد إشارات دقيقة دون الحاجة لتثبيت خادم خارجي.",
            "logout_btn" to "تسجيل خروج (إزالة الرخصة) 🚪",
            
            // Language Selection
            "lang_toggle" to "English (تغيير اللغة)"
        ),
        "en" to mapOf(
            "splash_subtitle" to "The Trading Wolf",
            "app_title" to "WOLF TRADER • Intelligent Signals",
            "app_subtitle" to "Smart algorithmic signaling system for professionals",
            "activate_license" to "Activate Software License",
            "activate_desc" to "Enter your activation key to log in and access the Wolf's trade signals.",
            "activation_key_label" to "Activation Key",
            "your_device_id" to "Your Device ID:",
            "activate_now_btn" to "Activate Now 🐺",
            "no_key" to "Don't have an activation key?",
            "no_key_desc" to "Contact the developer to purchase an instant PRO license.",
            "buy_key_btn" to "Buy Key via Telegram 🔗",
            "adv_server_settings" to "Advanced Server Settings (API Config)",
            "edit_trading_server" to "Edit Active Trading Server:",
            "demo_mode_label" to "Simulation Mode (Demo Mode)",
            "enabled" to "Enabled",
            "disabled" to "Disabled",
            "demo_hint" to "💡 For quick trial without external server: enable (Demo Mode) and enter key 'WOLF' to login.",
            
            // Errors
            "err_empty_key" to "⚠️ Please enter the activation key first",
            "err_invalid_key" to "❌ Invalid key",
            "err_expired_key" to "❌ Key expired",
            "err_device_mismatch" to "❌ Key used on another device",
            "err_mismatch" to "❌ Invalid or mismatched key",
            "err_connection_failed" to "❌ Connection failed. Check server URL or use Demo mode.",

            // Tabs
            "tab_signals" to "Signals",
            "tab_markets" to "Markets",
            "tab_chart" to "Chart",
            "tab_settings" to "Settings",

            // Chart Screen
            "chart_title" to "Quotex Platform • Live Chart",
            "chart_desc" to "Real-time candlestick price action stream with active trade entry markers.",
            "chart_no_active_deal" to "No active trade for this currency pair currently",
            "chart_active_deal_buy" to "Active BUY Upward Trade",
            "chart_active_deal_sell" to "Active SELL Downward Trade",
            "chart_entry_price" to "Asset entry point:",
            "chart_selector_prompt" to "Select asset pair to analyze:",
            "chart_live_badge" to "LIVE OTC",

            // HomeDashboard
            "signals_radar" to "Signals Radar",
            "demo_badge" to "Demo",
            "radar_desc" to "Smart Trend Analysis Signals",
            "active_lvl" to "Active Coverage Level",
            "active_lvl_desc" to "Found %d critical signals",
            "coverage_desc" to "Based on price action algorithms to track support/resistance trends.",
            "membership" to "Member",
            "strong" to "Very Strong 💪",
            "medium" to "Medium ⚡",
            "weak" to "Weak 🔍",
            "buy" to "BUY 🔥",
            "sell" to "SELL ❄️",
            "buy_direction" to "BUY",
            "sell_direction" to "SELL",
            "active_signal_label" to "Active Signal: %s (%d%%)",
            "entry_point" to "Entry Point:",
            "duration" to "Trade Duration:",
            "minutes" to "%d Min",
            "confidence_rate" to "Confidence Strength:",
            "expired_title" to "Expired",
            "result_title" to "Outcome:",
            "win_label" to "WIN ✅",
            "loss_label" to "LOSS ❌",
            "remaining" to "Remaining:",
            "waiting_signals" to "Waiting for signals...",
            "waiting_desc" to "The server is analyzing prices to discover opportunities. Make sure your internet connection is active, or enable Demo Mode.",
            "risk_warning_title" to "High-Risk Trading Disclaimer",
            "risk_warning_content" to "🚨 Trading involves major financial risk!\n1. Never enter any single deal with more than 2% (2/100) of your total account capital to safeguard your funds.",

            // MarketsScreen
            "markets_title" to "Financial Asset Markets",
            "markets_desc" to "Search, filter, and compare active signals with confidence scores.",
            "search_placeholder" to "Search pair (e.g. EUR/USD)",
            "favorites" to "Favorites",
            "cat_all" to "All 🌐",
            "cat_forex" to "Forex 💱",
            "cat_metals" to "Metals 🪙",
            "cat_crypto" to "Crypto 🪙",
            "cat_otc" to "OTC Market 🕰️",
            "no_active_signals" to "No active signals for this pair currently.",
            "no_results_found" to "No results found",
            "empty_fav_desc" to "Your favorites list is currently empty. Tap the heart to add pairs.",
            "empty_search_desc" to "No assets found matching '%s'. Enter a valid name like EUR/USD.",

            // SettingsScreen
            "settings_support_title" to "Settings & Support 🛠️",
            "account_license_details" to "License Details",
            "sub_type" to "Subscription Type",
            "sub_pro_value" to "Wolf Sovereign (PRO) - %s",
            "device_id_label" to "Device ID",
            "feed_health_label" to "Feed Status",
            "local_sim_active" to "Local Simulation Active ✅",
            "connected_to_active_server" to "Connected to Active Server 🌐",
            "social_channels" to "Subscriber Social Channels",
            "telegram_channel_title" to "Public Wolf Signals Channel (Telegram)",
            "telegram_channel_desc" to "Follow market updates and active trades shared with users.",
            "telegram_support_title" to "Tech Support & Activation Sales (Buy License)",
            "telegram_support_desc" to "Contact to purchase a license to unlock downstream signals.",
            "telegram_dev_title" to "Direct Developer Contact for Support & Feedback",
            "telegram_dev_desc" to "For technical issues or direct inquiries.",
            "advanced_tools" to "Advanced Tools",
            "base_url_label" to "Base Server URL",
            "base_url_desc" to "Update URL to connect to your custom production server.",
            "data_sim_title" to "Real-time Data Simulation",
            "data_sim_desc" to "Generate accurate signals without an external server.",
            "logout_btn" to "Log Out (Revoke License) 🚪",
            
            // Language Selection
            "lang_toggle" to "العربية (Toggle Language)"
        )
    )

    fun getString(lang: String, key: String, vararg args: Any): String {
        val bundle = translations[lang] ?: translations["ar"]!!
        val template = bundle[key] ?: return "[$key]"
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            template
        }
    }
}
