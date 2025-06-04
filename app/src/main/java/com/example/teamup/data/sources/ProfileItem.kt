package com.example.teamup.data.sources

import com.example.teamup.R
import com.example.teamup.data.model.ProfileItemModel

object ProfileItem {
    val data = listOf<ProfileItemModel>(
        ProfileItemModel(
            Id = 1,
            Icon = R.drawable.captain,
            Label = "Edit Profile",
            Route = "/"
        ),
//        ProfileItemModel(
//            Id = 2,
//            Icon = R.drawable.wallet,
//            Label = "Payment Option",
//            Route = "/"
//        ),
        ProfileItemModel(
            Id = 3,
            Icon = R.drawable.bell_icon,
            Label = "Notifications",
            Route = "/"
        ),
//        ProfileItemModel(
//            Id = 4,
//            Icon = R.drawable.shield,
//            Label = "Security",
//            Route = "/"
//        ),
//        ProfileItemModel(
//            Id = 5,
//            Icon = R.drawable.earth_asia,
//            Label = "Language",
//            Route = "/"
//        ),
        ProfileItemModel(
            Id = 6,
            Icon = R.drawable.share_nodes,
            Label = "Invite Friends",
            Route = "/"
        )
    )
}