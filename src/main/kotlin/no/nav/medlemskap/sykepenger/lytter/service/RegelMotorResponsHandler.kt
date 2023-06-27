package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar


class RegelMotorResponsHandler {
    fun interpretLovmeRespons(lovmeresponse: String) : FlexRespons {
        val lovmeresponseNode = objectMapper.readTree(lovmeresponse)
        when(lovmeresponseNode.svar()){
            "UAVKLART"->{return createFlexRespons(lovmeresponseNode)}
            "JA"->{return FlexRespons(svar = Svar.JA, emptySet())}
            "NEI"->{return FlexRespons(svar = Svar.NEI, emptySet())}
            else -> {throw IllegalStateException()}
        }




    }

    private fun createFlexRespons(lovmeresponseNode: JsonNode?) :FlexRespons{

        if (lovmeresponseNode!!.erEosBorger()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode!!.erTredjelandsborgerMedEØSFamilie() && lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode!!.erTredjelandsborgerMedEØSFamilie() && !lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode!!.erTredjelandsborger() && !lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE))
        }
        if (lovmeresponseNode!!.erTredjelandsborger() && lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE))
        }
         throw IllegalStateException()
    }
}
fun JsonNode.erEosBorger():Boolean{
    return this.finnSvarPaaRegel("REGEL_2")
}
fun JsonNode.finnSvarPaaRegel(regelID:String):Boolean{
    val regel = this.alleRegelResultat().finnRegel(regelID)
    if (regel!=null){
        return regel.get("svar").asText()=="JA"
    }
    return false
}
fun JsonNode.alleRegelResultat():List<JsonNode>{
    return this.get("resultat").get("delresultat").flatMap { it.get("delresultat")}
}
fun List<JsonNode>.finnRegel(regelID:String):JsonNode?{
    return this.find { it.get("regelId").asText()==regelID }
}
fun JsonNode.erTredjelandsborgerMedEØSFamilie():Boolean{
    return finnSvarPaaRegel("REGEL_28") && finnSvarPaaRegel("REGEL_29")
}
fun JsonNode.erTredjelandsborger():Boolean{
    return !this.finnSvarPaaRegel("REGEL_2")
}
fun JsonNode.erBritiskBorger():Boolean{
    return this.finnSvarPaaRegel("REGEL_19_7")
}
fun JsonNode.harOppholdsTilatelse():Boolean{
    /*
    * Sjekk uavklart svar fra UDI
    * */
    if (this.finnSvarPaaRegel("REGEL_19_1")){
        return false
    }
    /*
    * Sjekk Oppholdstilatelse tilbake i tid
    * */
    if (!this.finnSvarPaaRegel("REGEL_19_3")){
        return false
    }
    /*
    * Sjekk oppholdstilatelsen i  arbeidsperioden
    * */
    if (!this.finnSvarPaaRegel("REGEL_19_3_1")){
        return false
    }
    /*
     *Har bruker opphold på samme vilkår flagg?
     */
    if (this.finnSvarPaaRegel("REGEL_19_8")){
        return false

    }
    return true
}
fun JsonNode.svar():String{
    return this.get("resultat").get("svar").asText()
}