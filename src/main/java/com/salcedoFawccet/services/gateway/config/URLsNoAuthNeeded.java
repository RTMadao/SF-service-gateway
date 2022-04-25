package com.salcedoFawccet.services.gateway.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class URLsNoAuthNeeded {

    private List<Pattern> urlsExcludes;

    public URLsNoAuthNeeded() {
        this.urlsExcludes = new ArrayList<>();
        this.urlsExcludes.add(Pattern.compile("\\w*"+"/email/change_password/"+"\\w*"));
    }

    public boolean validateURL(String url){
        AtomicBoolean match = new AtomicBoolean(false);
        this.urlsExcludes.forEach(pattern -> {
            if (pattern.matcher(url).matches()) match.set(true);
        });
        return match.get();
    }
}
