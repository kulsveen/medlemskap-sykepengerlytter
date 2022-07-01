package no.nav.medlemskap.sykepenger.lytter.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import no.nav.medlemskap.saga.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.domain.*
import java.time.LocalDate
import java.time.LocalDateTime


class JacksonParser {
    private val log = KotlinLogging.logger { }
    private val secureLogger = KotlinLogging.logger("tjenestekall")
    fun parse(jsonString: String): LovmeSoknadDTO {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            secureLogger.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return LovmeSoknadDTO(
                "",
                SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.SENDT,
                "", null,
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDate.now(),
                LocalDate.now(), false
            )
        }
    }

    fun parseFlexBrukerSporsmaal(jsonString: String): FlexBrukerSporsmaal {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            secureLogger.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return FlexBrukerSporsmaal(null)
        }
    }

    fun parseMedlemskap(jsonString: String): Medlemskap {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            secureLogger.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }

    fun parseMedlemskap(medlemskap: Medlemskap): String {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.writeValueAsString(medlemskap)
        } catch (t: Throwable) {
            secureLogger.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }
    fun parseFlexBrukerSporsmaal(flexBrukerSporsmaal: FlexBrukerSporsmaal): String {
            try {
                val mapper: ObjectMapper = ObjectMapper()
                    .registerKotlinModule()
                    .findAndRegisterModules()
                    .configure(SerializationFeature.INDENT_OUTPUT, true)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                return mapper.writeValueAsString(flexBrukerSporsmaal)
            } catch (t: Throwable) {
                secureLogger.error("Unable to parse json. Dropping message. Cause : ${t.message}")
                throw t;
            }
        }
    fun parse(obj: Any): String {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.writeValueAsString(obj)
        } catch (t: Throwable) {
            secureLogger.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }
    fun ToJson(obj: Any): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return  mapper.valueToTree(obj);

    }
    fun ToJson(string: String): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return  mapper.readTree(string);

    }
}