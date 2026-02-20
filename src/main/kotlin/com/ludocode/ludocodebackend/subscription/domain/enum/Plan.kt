package com.ludocode.ludocodebackend.subscription.domain.enum

enum class Plan(val rank: Int) {
    FREE(0),
    SUPPORTER(1),
    PATRON(2);

    fun isUpgrade(from: Plan, to: Plan) = to.rank > from.rank
    fun isDowngrade(from: Plan, to: Plan) = to.rank < from.rank

}