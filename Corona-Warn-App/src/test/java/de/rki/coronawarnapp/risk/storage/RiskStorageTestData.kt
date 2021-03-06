package de.rki.coronawarnapp.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.risk.EwRiskLevelTaskResult
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDao
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import org.joda.time.Instant

object RiskStorageTestData {

    val ewCalculatedAt = Instant.ofEpochMilli(9999L)

    val testRiskLevelResultDao = PersistedRiskLevelResultDao(
        id = "riskresult-id",
        calculatedAt = ewCalculatedAt,
        failureReason = null,
        aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 2,
            mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
            mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
            numberOfDaysWithLowRisk = 5,
            numberOfDaysWithHighRisk = 6
        )
    )

    val testAggregatedRiskResult = EwAggregatedRiskResult(
        totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        totalMinimumDistinctEncountersWithLowRisk = 1,
        totalMinimumDistinctEncountersWithHighRisk = 2,
        mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
        mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
        numberOfDaysWithLowRisk = 5,
        numberOfDaysWithHighRisk = 6
    )

    val testRisklevelResult = EwRiskLevelTaskResult(
        calculatedAt = ewCalculatedAt,
        ewAggregatedRiskResult = testAggregatedRiskResult,
        exposureWindows = null
    )

    val testExposureWindowDaoWrapper = PersistedExposureWindowDaoWrapper(
        exposureWindowDao = PersistedExposureWindowDao(
            id = 1,
            riskLevelResultId = "riskresult-id",
            dateMillisSinceEpoch = 123L,
            calibrationConfidence = 1,
            infectiousness = 2,
            reportType = 3
        ),
        scanInstances = listOf(
            PersistedExposureWindowDao.PersistedScanInstance(
                exposureWindowId = 1,
                minAttenuationDb = 10,
                secondsSinceLastScan = 20,
                typicalAttenuationDb = 30
            )
        )
    )
    val testExposureWindow: ExposureWindow = ExposureWindow.Builder().apply {
        setDateMillisSinceEpoch(123L)
        setCalibrationConfidence(1)
        setInfectiousness(2)
        setReportType(3)
        ScanInstance.Builder().apply {
            setMinAttenuationDb(10)
            setSecondsSinceLastScan(20)
            setTypicalAttenuationDb(30)
        }.build().let { setScanInstances(listOf(it)) }
    }.build()

    val testAggregatedRiskPerDateResult = ExposureWindowDayRisk(
        dateMillisSinceEpoch = ewCalculatedAt.millis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 0
    )

    val testPersistedAggregatedRiskPerDateResult = PersistedAggregatedRiskPerDateResult(
        dateMillisSinceEpoch = ewCalculatedAt.millis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 0
    )

    val testRisklevelResultWithAggregatedRiskPerDateResult = testRisklevelResult.copy(
        ewAggregatedRiskResult = testAggregatedRiskResult.copy(
            exposureWindowDayRisks = listOf(testAggregatedRiskPerDateResult)
        )
    )
}
