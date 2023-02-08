package com.heyanle.lib_anim

import com.heyanle.bangumi_source_api.api.ISourceParser
import com.heyanle.bangumi_source_api.api.ParserLoader
import com.heyanle.lib_anim.agefans.AgefansParser
import com.heyanle.lib_anim.bimibimi.BimibimiParser
import com.heyanle.lib_anim.cycdm.CycdmParser
import com.heyanle.lib_anim.yhdm.YhdmParser
import com.heyanle.lib_anim.yhdmp.YhdmpParser

/**
 * Created by HeYanLe on 2023/2/1 15:55.
 * https://github.com/heyanLE
 */


object InnerLoader : ParserLoader {

    override fun load(): List<ISourceParser> {
        return listOf(
            CycdmParser(),
            YhdmParser(),
            YhdmpParser(),
            BimibimiParser(),
            AgefansParser(),
        )
    }
}