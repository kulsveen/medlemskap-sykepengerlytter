package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.saga.persistence.Brukersporsmaal
import no.nav.medlemskap.saga.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import java.time.LocalDate
import java.time.LocalDateTime

open class FlexMessageHandler (
    private val configuration: Configuration,
    private val persistenceService: PersistenceService,
    private val soknadRecordHandler: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val secureLogger = KotlinLogging.logger { }


    }

    suspend fun handle(flexMessageRecord: FlexMessageRecord) {
        handleBrukerSporsmaal(flexMessageRecord)
        handleLovmeRequest(flexMessageRecord)

    }

     suspend fun handleLovmeRequest(flexMessageRecord: FlexMessageRecord) {
        val requestObject = JacksonParser().parse(flexMessageRecord.value)
        if (soknadSkalSendesTeamLovMe(requestObject)){
            val soknadRecord =SoknadRecord(flexMessageRecord.partition,flexMessageRecord.offset,flexMessageRecord.value,flexMessageRecord.key,flexMessageRecord.topic,requestObject)
            soknadRecordHandler.handle(soknadRecord)
        }
    }

    fun soknadSkalSendesTeamLovMe(lovmeSoknadDTO: LovmeSoknadDTO) =
        lovmeSoknadDTO.status == SoknadsstatusDTO.SENDT &&
                lovmeSoknadDTO.type == SoknadstypeDTO.ARBEIDSTAKERE &&
                lovmeSoknadDTO.sendtNav != null && false == lovmeSoknadDTO.ettersending

    private fun handleBrukerSporsmaal(flexMessageRecord: FlexMessageRecord) {
        val brukersporsmaal: Brukersporsmaal = mapMessage(flexMessageRecord)

        if (brukersporsmaal.status == Soknadstatus.SENDT.toString()) {
            val existing = persistenceService.hentbrukersporsmaalForSoknadID(brukersporsmaal.soknadid)
            if (existing != null){
                log.info(
                    "Flex melding for søknad ${flexMessageRecord.key}, " +
                            "offset : ${flexMessageRecord.offset}, " +
                            "partition : ${flexMessageRecord.partition}," +
                            "filtrert ut. duplikat melding: ${brukersporsmaal.soknadid}"
                )
                return
            }
            persistenceService.lagreBrukersporsmaal(brukersporsmaal)
            log.info(
                "Brukerspørsmål for søknad ${flexMessageRecord.key} lagret til databasen",
                kv("callId", flexMessageRecord.key),
                kv("dato", brukersporsmaal.eventDate)
            )
        } else {
            log.info(
                "Flex melding for søknad ${flexMessageRecord.key}, " +
                        "offset : ${flexMessageRecord.offset}, " +
                        "partition : ${flexMessageRecord.partition}," +
                        "filtrert ut. Feil status : ${brukersporsmaal.status}"
            )
        }
    }

    open fun mapMessage(flexMessageRecord: FlexMessageRecord): Brukersporsmaal {
        try {
            val json = flexMessageRecord.value
            val JsonNode = ObjectMapper().readTree(json)
            val sporsmålArray = JsonNode.get("sporsmal")
            val fnr = JsonNode.get("fnr").asText()
            val status = JsonNode.get("status").asText()
            val type = JsonNode.get("type").asText()
            val id = JsonNode.get("id").asText()
            val sendtArbeidsgiver = JsonNode.get("sendtArbeidsgiver").asText(null)
            val sendtNav = JsonNode.get("sendtNav").asText(null)
            val sendtNavDato = parseDateString(sendtNav)
            val sendArbeidsgiverDato = parseDateString(sendtArbeidsgiver)

            var svarText: String = "IKKE OPPGITT"
            var svar: Boolean?
            val arbeidutland = sporsmålArray.find { it.get("tag").asText().equals("ARBEID_UTENFOR_NORGE") }
            if (arbeidutland != null) {
                //println(arbeidutland)
                try {
                    svarText = arbeidutland.get("svar").get(0).get("verdi").asText()
                } catch (t: Throwable) {

                }
            }
            if (svarText == "IKKE OPPGITT") {
                svar = null
            } else {
                if (svarText == "NEI") {
                    svar = false
                } else if (svarText == "JA") {
                    svar = true
                } else svar = null
            }

            return Brukersporsmaal(
                fnr,
                id,
                DatePicker().findEarliest(sendArbeidsgiverDato, sendtNavDato),
                "SYKEPENGER",
                status,
                FlexBrukerSporsmaal(svar)
            )
        }
        catch (t:Throwable){
            log.error("not able to parse message ${t.message}, cause : ${t.cause}")
            secureLogger.error("not able to parse message ${t.message}", kv("body",flexMessageRecord.value))
            throw t
        }
    }



    private fun parseDateString(dateString: String?): LocalDate? {
        return try {
            LocalDateTime.parse(dateString).toLocalDate()
        } catch (t: Throwable) {
            null
        }
    }

}

open class DatePicker(){
    companion object {
        private val log = KotlinLogging.logger { }
        private val secureLogger = KotlinLogging.logger("tjenestekall")

    }
    open fun findEarliest(sendArbeidsgiverDato: LocalDate?, sendtNavDato: LocalDate?): LocalDate {
        if (sendArbeidsgiverDato == null  && sendtNavDato == null){
            return LocalDate.now()
        }
        if (sendArbeidsgiverDato==null && sendtNavDato!=null){
            return sendtNavDato
        }
        if (sendtNavDato==null && sendArbeidsgiverDato !=null){
            return sendArbeidsgiverDato
        }
        if (sendtNavDato!!.isBefore(sendArbeidsgiverDato)){
            return sendtNavDato
        }
        else{
            return sendArbeidsgiverDato!!
        }

    }
}
