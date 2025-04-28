package com.mirkwood.novenapp.presentation.model

import com.mirkwood.novenapp.R

sealed class SocialMedia(val name: String, val url: String, val icon: Int) {
    object Facebook :
        SocialMedia("Facebook", "https://www.facebook.com/", R.drawable.ic_facebook_secondary)

    object Twitter : SocialMedia("Twitter", "https://twitter.com/", R.drawable.ic_x)
    object Instagram :
        SocialMedia("Instagram", "https://www.instagram.com/", R.drawable.ic_instagram)

    //object TikTok : SocialMedia("TikTok", "https://www.tiktok.com/", R.drawable.ic_whatsapp)
    //object YouTube : SocialMedia("YouTube", "https://www.youtube.com/", R.drawable.ic_yt)


    companion object {
        val all = listOf(Facebook, Instagram, Twitter)
    }
}
