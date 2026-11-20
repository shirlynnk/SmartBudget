# SmartBudget Styling Reference

This reference collects the styling resources currently used by the app. It is a readable snapshot; the Android resource files under `app/src/main/res` remain the source used by the build.

## Design palette

Source: `app/src/main/res/values/colors.xml`

```xml
<resources>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="obsidian_background">#13191D</color>
    <color name="card_background">#26343B</color>
    <color name="deep_teal">#328C89</color>
    <color name="turquoise">#83C6BD</color>
    <color name="obsidian_text">#E7F0EF</color>
    <color name="drawer_track">#52616A</color>
    <color name="dashboard_divider">#3A83C6BD</color>
</resources>
```

Dashboard chart colors from `app/src/main/res/values/dashboard_colors.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="chart_track">#354C54</color>
    <color name="chart_blue">#5CBBC2</color>
    <color name="chart_amber">#F2AE58</color>
    <color name="chart_mint">#9BC9CB</color>
    <color name="chart_red">#F07870</color>
</resources>
```

## Typography and app theme

Source: `app/src/main/res/values/themes.xml`

Page titles use a bold serif face. Section titles use condensed bold sans serif. The toolbar uses a heavy sans serif face, while body text stays sans serif.

```xml
<resources>
    <style name="TextAppearance.SmartBudget" parent="@android:style/TextAppearance">
        <item name="android:fontFamily">sans-serif</item>
    </style>

    <style name="TextAppearance.SmartBudget.PageTitle">
        <item name="android:fontFamily">serif</item>
        <item name="android:textStyle">bold</item>
        <item name="android:letterSpacing">-0.015</item>
    </style>

    <style name="TextAppearance.SmartBudget.SectionTitle">
        <item name="android:fontFamily">sans-serif-condensed</item>
        <item name="android:textStyle">bold</item>
        <item name="android:letterSpacing">0.01</item>
    </style>

    <style name="TextAppearance.SmartBudget.ToolbarTitle">
        <item name="android:fontFamily">sans-serif-black</item>
        <item name="android:textStyle">bold</item>
    </style>

    <style name="TextAppearance.SmartBudget.Kicker">
        <item name="android:fontFamily">sans-serif-condensed-medium</item>
        <item name="android:textStyle">bold</item>
        <item name="android:letterSpacing">0.14</item>
    </style>

    <style name="TextAppearance.SmartBudget.Body">
        <item name="android:fontFamily">sans-serif</item>
    </style>

    <style name="Base.Theme.TempBudget" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:fontFamily">sans-serif</item>
        <item name="colorPrimary">@color/deep_teal</item>
        <item name="colorPrimaryDark">@color/obsidian_background</item>
        <item name="colorOnPrimary">@color/obsidian_text</item>
        <item name="colorSecondary">@color/turquoise</item>
        <item name="colorOnSecondary">@color/obsidian_background</item>
        <item name="colorSurface">@color/card_background</item>
        <item name="colorOnSurface">@color/obsidian_text</item>
        <item name="android:windowBackground">@color/obsidian_background</item>
        <item name="android:statusBarColor">@color/obsidian_background</item>
        <item name="android:navigationBarColor">@color/obsidian_background</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
    <style name="Theme.TempBudget" parent="Base.Theme.TempBudget" />
</resources>
```

These text appearances are used in the page layouts for Dashboard, Expenses, My Budget, Categories, Reports, Exchange Rates, Reminders, Settings/About, and the add/edit expense screen.

## Dashboard wallpaper

Source: `app/src/main/res/drawable/bg_smartbudget_wallpaper.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item>
        <shape android:shape="rectangle">
            <gradient
                android:angle="270"
                android:startColor="#172126"
                android:centerColor="#13191D"
                android:endColor="#101619"
                android:type="linear" />
        </shape>
    </item>
    <item android:drawable="@drawable/bg_fintech_pattern" />
</layer-list>
```

Source: `app/src/main/res/drawable/bg_fintech_pattern.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="360dp"
    android:height="800dp"
    android:viewportWidth="360"
    android:viewportHeight="800">
    <path android:fillColor="#00000000" android:pathData="M300,24a62,62 0,1 0,0 124a62,62 0,1 0,0 -124" android:strokeColor="#83C6BD" android:strokeAlpha="0.10" android:strokeWidth="1.4" />
    <path android:fillColor="#00000000" android:pathData="M300,48a38,38 0,1 0,0 76a38,38 0,1 0,0 -76" android:strokeColor="#328C89" android:strokeAlpha="0.14" android:strokeWidth="1.2" />
    <path android:fillColor="#00000000" android:pathData="M-24,377 C24,340 64,354 95,330 C125,306 149,316 177,293" android:strokeColor="#83C6BD" android:strokeAlpha="0.11" android:strokeWidth="1.3" />
    <path android:fillColor="#00000000" android:pathData="M-24,389 C24,352 64,366 95,342 C125,318 149,328 177,305" android:strokeColor="#328C89" android:strokeAlpha="0.08" android:strokeWidth="1" />
    <path android:fillColor="#328C89" android:fillAlpha="0.07" android:pathData="M237,548h14v48h-14zM258,522h14v74h-14zM279,490h14v106h-14zM300,460h14v136h-14zM321,438h14v158h-14z" />
    <path android:fillColor="#00000000" android:pathData="M232,553l28,-30 21,-31 21,-29 21,-20 24,-20" android:strokeColor="#83C6BD" android:strokeAlpha="0.13" android:strokeWidth="1.4" />
    <path android:fillColor="#00000000" android:pathData="M29,653c0,-11 9,-20 20,-20h49c11,0 20,9 20,20v41c0,11 -9,20 -20,20H49c-11,0 -20,-9 -20,-20z" android:strokeColor="#83C6BD" android:strokeAlpha="0.09" android:strokeWidth="1.5" />
    <path android:fillColor="#00000000" android:pathData="M29,672h89" android:strokeColor="#83C6BD" android:strokeAlpha="0.09" android:strokeWidth="1.5" />
    <path android:fillColor="#328C89" android:fillAlpha="0.08" android:pathData="M91,666a10,10 0,1 0,0 20a10,10 0,1 0,0 -20" />
    <path android:fillColor="#00000000" android:pathData="M226,711a4,4 0,1 0,0 8a4,4 0,1 0,0 -8M246,711a4,4 0,1 0,0 8a4,4 0,1 0,0 -8M266,711a4,4 0,1 0,0 8a4,4 0,1 0,0 -8M286,711a4,4 0,1 0,0 8a4,4 0,1 0,0 -8M306,711a4,4 0,1 0,0 8a4,4 0,1 0,0 -8" android:strokeColor="#83C6BD" android:strokeAlpha="0.12" android:strokeWidth="1" />
</vector>
```

## Card and selection backgrounds

Source: `app/src/main/res/drawable/bg_dashboard_hero.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <corners android:radius="26dp" />
    <gradient android:angle="0" android:startColor="#1C565B" android:centerColor="#17434A" android:endColor="#26343B" android:type="linear" />
    <stroke android:width="1dp" android:color="#6683C6BD" />
</shape>
```

Source: `app/src/main/res/drawable/bg_exchange_result.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#1F328C89" />
    <corners android:radius="18dp" />
    <stroke android:width="1dp" android:color="#4083C6BD" />
</shape>
```

Source: `app/src/main/res/drawable/bg_feature_icon.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#1F328C89" />
    <corners android:radius="16dp" />
    <stroke android:width="1dp" android:color="#4083C6BD" />
</shape>
```

Source: `app/src/main/res/drawable/bg_expense_icon.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="#33328C89" />
    <stroke android:width="1dp" android:color="#55328C89" />
</shape>
```

Source: `app/src/main/res/drawable/drawer_item_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_checked="true">
        <shape android:shape="rectangle">
            <solid android:color="#40328C89" />
            <corners android:topLeftRadius="18dp" android:bottomLeftRadius="18dp" />
        </shape>
    </item>
    <item android:drawable="@android:color/transparent" />
</selector>
```

## Brand vector artwork

Source: `app/src/main/res/drawable/ic_smartbudget_logo.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="64dp"
    android:height="64dp"
    android:viewportWidth="64"
    android:viewportHeight="64">
    <path android:fillColor="#328C89" android:pathData="M12,2h40c5.52,0 10,4.48 10,10v40c0,5.52 -4.48,10 -10,10h-40c-5.52,0 -10,-4.48 -10,-10v-40c0,-5.52 4.48,-10 10,-10z" />
    <path android:fillColor="#E7F0EF" android:pathData="M13,22c0,-3.31 2.69,-6 6,-6h25c3.31,0 6,2.69 6,6v23c0,2.76 -2.24,5 -5,5H18c-2.76,0 -5,-2.24 -5,-5z" />
    <path android:fillColor="#26343B" android:pathData="M13,28h37v12c0,2.76 -2.24,5 -5,5H18c-2.76,0 -5,-2.24 -5,-5z" />
    <path android:fillColor="#83C6BD" android:pathData="M37,30h15v12H37c-3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6z" />
    <path android:fillColor="#E7F0EF" android:pathData="M38,34a2,2 0,1 0,0 4a2,2 0,1 0,0 -4" />
    <path android:fillColor="#83C6BD" android:pathData="M18,23h7v2h-7zM28,20h3v5h-3zM34,17h3v8h-3z" />
</vector>
```

Source: `app/src/main/res/drawable/ill_coins_growth.xml`  
This vector draws the coin stacks and growing plant used in the dashboard. The production drawable contains the full paths; it is kept separately in the project so the app can load it as a drawable.

Source: `app/src/main/res/drawable/ic_menu.xml`  
Teal three-line drawer icon used by the app toolbar.

Source: `app/src/main/res/drawable/ic_launcher_background.xml` and `ic_launcher_foreground.xml`  
Adaptive launcher icon background and wallet foreground.

## Where the styles are applied

- `TextAppearance.SmartBudget.PageTitle`: screen headings and the dashboard SmartBudget wordmark.
- `TextAppearance.SmartBudget.SectionTitle`: section headings within screens.
- `TextAppearance.SmartBudget.Kicker`: small uppercase section labels.
- `TextAppearance.SmartBudget.Body`: explanatory and supporting text.
- `TextAppearance.SmartBudget.ToolbarTitle`: app and form toolbars.
- `bg_smartbudget_wallpaper`: screen backgrounds.
- `bg_dashboard_hero`: dashboard budget hero card.
- `ic_smartbudget_logo`: dashboard and drawer branding.
- `ill_coins_growth`: dashboard coins-and-plant illustration.
- `drawer_item_background`: selected drawer destination.
