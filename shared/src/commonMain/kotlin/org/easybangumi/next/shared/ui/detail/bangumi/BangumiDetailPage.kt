package org.easybangumi.next.shared.ui.detail.bangumi

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
//@Composable
//fun BangumiDetailPage(
//    cartoonIndex: CartoonIndex,
//    metaBusiness: ComponentBusiness<BangumiMetaComponent>,
//) {
//
//    val navController = LocalNavController.current
//    val vm = vm(
//        ::BangumiDetailViewModel, cartoonIndex, metaBusiness
//    )
//    val appBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        BangumiDetail(
//            modifier = Modifier.fillMaxSize(),
//            vm = vm,
//            nestedScrollConnection = appBarBehavior.nestedScrollConnection,
//            contentPaddingTop = 64.dp,
//        )
//        BangumiDetailTopBar(vm, navController, appBarBehavior)
//    }
//
//}
//
//
//@Composable
//fun BangumiDetailTopBar(
//    vm: BangumiDetailViewModel,
//    navController: NavController,
//    behavior: TopAppBarScrollBehavior? = null,
//) {
//    TopAppBar(
//        title = {},
//        navigationIcon = {
//            IconButton(onClick = {
//                navController.navigateUp()
//            }) {
//                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
//            }
//        },
//        scrollBehavior = behavior,
//        colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent)
//    )
//
//}