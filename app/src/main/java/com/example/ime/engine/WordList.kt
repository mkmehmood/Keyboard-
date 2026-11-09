package com.example.ime.engine

/** Small built-in English word list, most frequent first. Original compilation, extend as needed. */
object WordList {
    val COMMON: List<String> = (
        "the be to of and a in that have i it for not on with he as you do at this but his by from they we say her she or an " +
        "will my one all would there their what so up out if about who get which go me when make can like time no just him " +
        "know take people into year your good some could them see other than then now look only come its over think also " +
        "back after use two how our work first well way even new want because any these give day most us is are was were " +
        "been has had did does done am very much more many should may might must shall where why here too still home " +
        "life world school state family student group country problem hand part place case week company system program " +
        "question government number night point city play small end put read ask need feel try leave call keep let begin " +
        "seem help talk turn start show hear run move live believe hold bring happen write provide sit stand lose pay " +
        "meet include continue set learn change lead understand watch follow stop create speak allow add spend grow open " +
        "walk win offer remember love consider appear buy wait serve die send expect build stay fall cut reach kill " +
        "remain thanks thank please sorry hello hi hey okay yes yeah yep nope maybe sure great nice cool awesome happy " +
        "today tomorrow yesterday tonight morning evening later soon always never often again already really right " +
        "left long little big old young high different next early large important same able best better last great " +
        "message phone call text email meeting dinner lunch breakfast coffee water food friend mother father brother " +
        "sister wife husband child children man woman boy girl money job business house room car door street town " +
        "again another between through during before under while both each every off down away around against without " +
        "within along though although until since whether either neither however therefore otherwise anyway actually " +
        "probably definitely absolutely currently recently finally basically literally honestly seriously exactly " +
        "people thing things something anything everything nothing someone anyone everyone nobody going doing being " +
        "having getting making coming looking working trying thinking saying taking wanting giving finding telling " +
        "asking seeming feeling leaving calling keeping letting meaning running moving playing paying hearing " +
        "i'm i'll i've i'd don't doesn't didn't can't couldn't won't wouldn't shouldn't isn't aren't wasn't weren't " +
        "it's that's what's there's here's let's he's she's they're we're you're they've we've you've"
    ).split(" ").filter { it.isNotBlank() }.distinct()
}
