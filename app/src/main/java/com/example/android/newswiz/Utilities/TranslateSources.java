package com.example.android.newswiz.Utilities;

public class TranslateSources {

    public static String translateNewsPublisher(String newsPublisher){
        switch (newsPublisher){

            case "ABC News":
                return "abc-news";
            case "ABC News (AU)":
                return "abc-news-au";
            case "Aftenposten":
                return "aftenposten";
            case "AlJazeera (ENG)":
                return "al-jazeera-english";
            case "BBC":
                return "bbc-news";
            case "CBS News":
                return "cbs-news";
            case "CNN News":
                return "cnn";
            case "Entertainment Weekly":
                return "entertainment-weekly";
            case "ESPN":
                return "espn";
            case "Financial Post":
                return "financial-post";
            case "Financial Times":
                return "financial-times";
            case "Fox News":
                return "fox-news";
            case "Fox Sports":
                return "fox-sports";
            case "IGN":
                return "ign";
            case "Independent":
                return "independent";
            case "L'Equipe":
                return "lequipe";
            case "Metro":
                return "metro";
            case "MSNBC":
                return "msnbc";
            case "MTV News":
                return "mtv-news";
            case "Nat. Geo.":
                return "national-geographic";
            case "NBC News":
                return "nbc-news";
            case "New Scientist":
                return "new-scientist";
            case "NY Magazine":
                return "new-york-magazine";
            case "Talk Sport":
                return "talksport";
            case "TechRadar":
                return "techradar";
            case "The Guardian":
                return "the-guardian-uk";
            case "NYT":
                return "the-new-york-times";
            case "Wall Street Journal":
                return "the-wall-street-journal";
            default:
                return "No Match";
        }

    }

    public static String translateCountry(String country){
        switch (country){
            case "Argentina":
                return "ar";
            case "Australia":
                return "au";
            case "Austria":
                return "at";
            case "Belgium":
                return "be";
            case "Brazil":
                return "br";
            case "Bulgaria":
                return "bg";
            case "Canada":
                return "ca";
            case "China":
                return "cn";
            case "Colombia":
                return "co";
            case "Cuba":
                return "cu";
            case "Czech Rep.":
                return "cz";
            case "Egypt":
                return "eg";
            case "France":
                return "fr";
            case "Germany":
                return "de";
            case "Greece":
                return "gr";
            case "Hong Kong":
                return "hk";
            case "Hungary":
                return "hu";
            case "India":
                return "in";
            case "Indonesia":
                return "id";
            case "Ireland":
                return "ie";
            case "Israel":
                return "il";
            case "Italy":
                return "it";
            case "Japan":
                return "jp";
            case "Latvia":
                return "lv";
            case "Lithuania":
                return "lt";
            case "Malaysia":
                return "my";
            case "Mexico":
                return "mx";
            case "Morocco":
                return "ma";
            case "Netherlands":
                return "nl";
            case "New Zealand":
                return "nz";
            case "Nigeria":
                return "ng";
            case "Norway":
                return "no";
            case "Philippines":
                return "ph";
            case "Poland":
                return "pl";
            case "Portugal":
                return "pt";
            case "Romania":
                return "ro";
            case "Russia":
                return "ru";
            case "Saudi Arabia":
                return "sa";
            case "Serbia":
                return "rs";
            case "Singapore":
                return "sg";
            case "Slovakia":
                return "sk";
            case "Slovenia":
                return "si";
            case "South Africa":
                return "za";
            case "South Korea":
                return "kr";
            case "Sweden":
                return "se";
            case "Switzerland":
                return "ch";
            case "Taiwan":
                return "tw";
            case "Thailand":
                return "th";
            case "Turkey":
                return "tr";
            case "Ukraine":
                return "ua";
            case "U.A.E":
                return "ae";
            case "U.K":
                return "gb";
            case "U.S.A":
                return "us";
            case "Venezuela":
                return "ve";
            default: return "No Match";
        }
    }

    public static String translateCategory(String category){
        switch (category){
            case "Business":
                return "business";
            case "Entertainment":
                return "entertainment";
            case "Health":
                return "health";
            case "Science":
                return "science";
            case "Sports":
                return "sports";
            case "Technology":
                return "technology";
            default:
                return "No Match";
        }
    }

    public static String formatPublishedDate(String publishedAtDate){
        String[] parts = publishedAtDate.split("T");
        String YYYYmmDD = parts[0];

        String[] parts2 = YYYYmmDD.split("-");
        String YYYY = parts2[0];
        String mm = parts2[1];
        String DD = parts2[2];

        String month = translateMonth(mm);

        return DD + " " + month + " " + YYYY;
    }

    private static String translateMonth(String mm){
        switch (mm){
            case "01":
                return "January";
            case "02":
                return "February";
            case "03":
                return "March";
            case "04":
                return "April";
            case "05":
                return "May";
            case "06":
                return "June";
            case "07":
                return "July";
            case "08":
                return "August";
            case "09":
                return "September";
            case "10":
                return "October";
            case "11":
                return "November";
            case "12":
                return "December";
            default: return "No month";
        }
    }
}
