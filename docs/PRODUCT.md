# Product Specification

## Product
**Kshetrajna: Smart Soil Intelligence & Safe Drip System**

## Goal
Integrated IoT + AI precision agriculture intended to optimize water/nutrient usage and address operational hazards on smallholder farms without continuous Internet dependence.

## Android scope
- farm/node overview
- soil telemetry
- manual pH
- fertility/NPK inference
- weather context
- irrigation status/approved controls
- alerts/safety explanations
- offline operation
- BLE
- backend synchronization

## Project inputs
LoRa telemetry: soil moisture, soil temperature, EC.
Atmospheric data: air temperature, humidity.
External/manual: rainfall/weather forecast and manual pH.

## NPK
EC and soil temperature are measured inputs; soil moisture is contextual; pH is app-assisted/manual; N/P/K are inferred outputs.

## Safety scope
Project architecture includes stray-current/soil-fault protection, root-zone thermal-shock protection, and inline salinity/fertigation protection.

Exact thresholds and calibration values are not specified here.

## Out of scope until approved
Exact crop limits, NPK equations/model coefficients, API endpoints, BLE protocol details, irrigation timing constants, and safety thresholds.
