package com.heyanle.easy_bangumi_cm.model.meida.local

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.ResolutionRule
import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.StackedFileRule
import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.StubType
import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.StubTypeRule

// 此处来自 Emby.Naming.NamingOptions

var videoFileExtensions = arrayOf(
    ".001",
    ".3g2",
    ".3gp",
    ".amv",
    ".asf",
    ".asx",
    ".avi",
    ".bin",
    ".bivx",
    ".divx",
    ".dv",
    ".dvr-ms",
    ".f4v",
    ".fli",
    ".flv",
    ".ifo",
    ".img",
    ".iso",
    ".m2t",
    ".m2ts",
    ".m2v",
    ".m4v",
    ".mkv",
    ".mk3d",
    ".mov",
    ".mp4",
    ".mpe",
    ".mpeg",
    ".mpg",
    ".mts",
    ".mxf",
    ".nrg",
    ".nsv",
    ".nuv",
    ".ogg",
    ".ogm",
    ".ogv",
    ".pva",
    ".qt",
    ".rec",
    ".rm",
    ".rmvb",
    ".strm",
    ".svq3",
    ".tp",
    ".ts",
    ".ty",
    ".viv",
    ".vob",
    ".vp3",
    ".webm",
    ".wmv",
    ".wtv",
    ".xvid"
)

var subtitleFileExtensions = arrayOf(
    ".ass",
    ".mks",
    ".sami",
    ".smi",
    ".srt",
    ".ssa",
    ".sub",
    ".sup",
    ".vtt",
)

var audioFileExtensions = arrayOf(
    ".669",
    ".3gp",
    ".aa",
    ".aac",
    ".aax",
    ".ac3",
    ".act",
    ".adp",
    ".adplug",
    ".adx",
    ".afc",
    ".amf",
    ".aif",
    ".aiff",
    ".alac",
    ".amr",
    ".ape",
    ".ast",
    ".au",
    ".awb",
    ".cda",
    ".cue",
    ".dmf",
    ".dsf",
    ".dsm",
    ".dsp",
    ".dts",
    ".dvf",
    ".far",
    ".flac",
    ".gdm",
    ".gsm",
    ".gym",
    ".hps",
    ".imf",
    ".it",
    ".m15",
    ".m4a",
    ".m4b",
    ".mac",
    ".med",
    ".mka",
    ".mmf",
    ".mod",
    ".mogg",
    ".mp2",
    ".mp3",
    ".mpa",
    ".mpc",
    ".mpp",
    ".mp+",
    ".msv",
    ".nmf",
    ".nsf",
    ".nsv",
    ".oga",
    ".ogg",
    ".okt",
    ".opus",
    ".pls",
    ".ra",
    ".rf64",
    ".rm",
    ".s3m",
    ".sfx",
    ".shn",
    ".sid",
    ".stm",
    ".strm",
    ".ult",
    ".uni",
    ".vox",
    ".wav",
    ".wma",
    ".wv",
    ".xm",
    ".xsp",
    ".ymf",
)

var ebookFileExtensions = arrayOf(
    ".mobi",
    ".epub",
    ".txt",
    ".pdf",
)

var imageFileExtensions = arrayOf(
    ".png",
    ".jpg",
    ".jpeg",
    ".gif",
    ".bmp",
    ".tiff",
    ".webp"
)

var mediaFlagDelimiters = arrayOf(
    '(',
    ')',
    '[',
    ']',
    '_',
    '.',
    '-',
)

var stubTypes = arrayOf(
    StubTypeRule(
        type = StubType.DVD,
        token = "dvd"
    ),
    StubTypeRule(
        type = StubType.HDDVD,
        token = "hddvd"
    ),
    StubTypeRule(
        type = StubType.BLURAY,
        token = "bluray"
    ),
    StubTypeRule(
        type = StubType.BLURAY,
        token = "brrip"
    ),
    StubTypeRule(
        type = StubType.BLURAY,
        token = "bd25"
    ),
    StubTypeRule(
        type = StubType.BLURAY,
        token = "bd50"
    ),
    StubTypeRule(
        type = StubType.VHS,
        token = "vhs"
    ),
    StubTypeRule(
        type = StubType.TV,
        token = "HDTV"
    ),
    StubTypeRule(
        type = StubType.TV,
        token = "PDTV"
    ),
    StubTypeRule(
        type = StubType.TV,
        token = "DSR"
    ),
    StubTypeRule(
        type = StubType.WEB,
        token = "WEB"
    ),
    StubTypeRule(
        type = StubType.WEB,
        token = "WEB-DL"
    ),
    StubTypeRule(
        type = StubType.WEB,
        token = "WEBRip"
    ),
)

var resolutionType = arrayOf(
    ResolutionRule(
        token = "1080p",
        width = 1920,
        height = 1080
    ),
    ResolutionRule(
        token = "720p",
        width = 1280,
        height = 720
    ),
    ResolutionRule(
        token = "480p",
        width = 854,
        height = 480
    ),
    ResolutionRule(
        token = "360p",
        width = 640,
        height = 360
    ),
    ResolutionRule(
        token = "240p",
        width = 426,
        height = 240
    ),
    ResolutionRule(
        token = "2160p",
        width = 3840,
        height = 2160
    ),
    ResolutionRule(
        token = "1440p",
        width = 2560,
        height = 1440
    ),
    ResolutionRule(
        token = "4k",
        width = 3840,
        height = 2160
    ),
    ResolutionRule(
        token = "8k",
        width = 7680,
        height = 4320
    ),
    ResolutionRule(
        token = "2k",
        width = 2048,
        height = 1080
    ),
)

val videoStackedFileRules = arrayOf(
    StackedFileRule("^(?<filename>.*?)(?:(?<=[\\]\\)\\}])|[ _.-]+)[\\(\\[]?(?<parttype>cd|dvd|part|pt|dis[ck])[ _.-]*(?<number>[0-9]+)[\\)\\]]?(?:\\.[^.]+)?$")
)