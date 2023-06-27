package no.nav.medlemskap.sykepenger.lytter.service

import io.ktor.server.plugins.statuspages.*
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Collections

class RegelMotorResponsHandlerTest {
    @Test
    fun eosborger(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_EOS_BORGER.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.erEosBorger())
        Assertions.assertFalse(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())


    }
    @Test
    fun tredjelandsBorgerGiftMedEOSPerson(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_GIFTEOS_BORGER.json").readText(Charsets.UTF_8)
        val response = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(response.svar == (Svar.UAVKLART))
        Assertions.assertTrue(response.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE, Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)))
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertTrue(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun tredjelandsBorgerIkkeGiftMedEOSPerson(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_IKKE_GIFT_EOS_BORGER.json").readText(Charsets.UTF_8)
        val response = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(response.svar == (Svar.UAVKLART))
        Assertions.assertTrue(response.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE, Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE)))
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun regel_19_1(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_1.json").readText(Charsets.UTF_8)
        val response  = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(response.svar == (Svar.UAVKLART))
        Assertions.assertTrue(response.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE, Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE)))
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun regel_19_8(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_8.json").readText(Charsets.UTF_8)
        val response = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(response.svar == (Svar.UAVKLART))
        Assertions.assertTrue(response.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE, Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE)))
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun regel_19_7(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_7.json").readText(Charsets.UTF_8)
        val response = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertTrue(response.svar == (Svar.UAVKLART))
        Assertions.assertTrue(response.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE, Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)))
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.erBritiskBorger())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun REGEL_0_1(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_0_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erBritiskBorger())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }

}