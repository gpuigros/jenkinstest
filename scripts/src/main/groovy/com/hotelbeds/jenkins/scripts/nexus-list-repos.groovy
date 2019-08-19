//@Autoapproved


import groovy.util.XmlSlurper


def findSolutionsByUrl = { url ->
    def solutionAlias = [] as Set
    def repos = new XmlSlurper().parseText(new URL(url).text)

    repos.data.'repositories-item'.each { repo ->
        def effectiveLocalStorageUrl = repo.effectiveLocalStorageUrl.toString().replaceAll('file:///opt/repositories/.*/', '')
        effectiveLocalStorageUrl = effectiveLocalStorageUrl.replaceAll('-repo', '')
        if (!effectiveLocalStorageUrl.toString().startsWith('file:/') && !effectiveLocalStorageUrl.toString().startsWith('http')) {
            solutionAlias << effectiveLocalStorageUrl
        }
    }

    return solutionAlias
}


def solutions = [] as Set

//TODO: Imports no funcionarán en los jobs generators de config. Hay que pasar lo que generan las siguientes lineas como parámetro del script.
//Business.values().each { business ->
//    Provider.values().each { provider ->
//        Region.values().each { region ->
//            def nexusSharedUrl = "https://nexus-repository.${Environment.SHARED.code}-${business.code}-${provider.code}-${region.code}.hotelbeds.com/nexus/content/repositories"
//            solutions.addAll(findSolutionsByUrl(nexusSharedUrl))
//        }
//    }
//}

def sharedPalmaUrl = 'https://nexus-repository.shared-hbg-hbg-palma.hotelbeds.com/nexus/service/local/repositories'
def sharedBtUrl = 'https://nexus-repository.shared-hbg-bt-madrid.hotelbeds.com/nexus/service/local/repositories'
def sharedAwsUrl = 'https://nexus-repository.shared-hbg-aws-eu-west-1.hotelbeds.com/nexus/service/local/repositories'

solutions.addAll(findSolutionsByUrl(sharedPalmaUrl))
solutions.addAll(findSolutionsByUrl(sharedBtUrl))
solutions.addAll(findSolutionsByUrl(sharedAwsUrl))
solutions.sort()
