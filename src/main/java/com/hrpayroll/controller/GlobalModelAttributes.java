package com.hrpayroll.controller;

import com.hrpayroll.util.AssetVersion;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Exposes the cache-busting asset version (see AssetVersion - benign, primitive #2) to every JSP. */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("appAssetVersion")
    public String appAssetVersion() {
        return AssetVersion.suffix();
    }
}
