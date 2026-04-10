package com.ash.mahjong.data.player

sealed interface AddPlayerResult {
    data object Success : AddPlayerResult
    data object InvalidName : AddPlayerResult
    data object DuplicateName : AddPlayerResult
}
