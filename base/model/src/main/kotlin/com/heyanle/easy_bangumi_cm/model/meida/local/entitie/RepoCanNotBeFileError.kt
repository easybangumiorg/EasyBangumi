package com.heyanle.easy_bangumi_cm.model.meida.local.entitie

class RepoCanNotBeFileError(msg: String) : Throwable() {
    override val message: String
        get() = "Repo can not be file"
}