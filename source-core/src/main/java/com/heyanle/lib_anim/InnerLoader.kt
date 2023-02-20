package com.heyanle.lib_anim

import com.heyanle.bangumi_source_api.api.ISourceParser
import com.heyanle.bangumi_source_api.api.ParserLoader
import com.heyanle.lib_anim.agefans.AgefansParser
import com.heyanle.lib_anim.bimibimi.BimibimiParser
import com.heyanle.lib_anim.cycdm.CycdmParser
import com.heyanle.lib_anim.cycplus.CycplusParser
import com.heyanle.lib_anim.omofun.OmofunParser
import com.heyanle.lib_anim.yhdm.YhdmParser
import com.heyanle.lib_anim.yhdmp.YhdmpParser

/**
 * Created by HeYanLe on 2023/2/1 15:55.
 * https://github.com/heyanLE
 */


object InnerLoader : ParserLoader {

    override fun load(): List<ISourceParser> {
        return listOf(
            OmofunParser(),     // Omofun
            CycplusParser(),    // 次元城+
            CycdmParser(),      // 次元城
            YhdmParser(),       // 樱花动漫
            YhdmpParser(),      // 樱花动漫 P
            BimibimiParser(),   // Bimibimi
            AgefansParser(),    // Age
        )
    }
}