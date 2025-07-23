use btleplug::api::{Central, Manager as _, Peripheral as _, ScanFilter, WriteType};
use btleplug::platform::Manager;
use tokio::time::{sleep, Duration};
use uuid::Uuid;

/// Default service UUID used for discovering remote devices.
const SERVICE_UUID: Uuid = Uuid::from_u128(0xfeed_cafe_dead_beef_feed_cafe_dead_beef);
/// Characteristic UUID used for message writes.
const CHARACTERISTIC_UUID: Uuid = Uuid::from_u128(0xfeed_cafe_dead_beef_feed_cafe_dead_bead);

/// Simple BLE client for pairing and sending data.
pub struct BleClient {
    service_uuid: Uuid,
    char_uuid: Uuid,
}

impl Default for BleClient {
    fn default() -> Self {
        Self { service_uuid: SERVICE_UUID, char_uuid: CHARACTERISTIC_UUID }
    }
}

impl BleClient {
    /// Scan for the first peripheral advertising the service and connect.
    pub async fn pair(&self) -> anyhow::Result<()> {
        let manager = Manager::new().await?;
        let adapter = manager
            .adapters()
            .await?
            .into_iter()
            .next()
            .ok_or_else(|| anyhow::anyhow!("No Bluetooth adapters found"))?;
        adapter.start_scan(ScanFilter::default()).await?;
        sleep(Duration::from_secs(2)).await;
        for peripheral in adapter.peripherals().await? {
            if let Some(props) = peripheral.properties().await? {
                if props.services.iter().any(|s| *s == self.service_uuid) {
                    peripheral.connect().await?;
                    peripheral.disconnect().await?;
                    return Ok(());
                }
            }
        }
        Err(anyhow::anyhow!("Matching BLE device not found"))
    }

    /// Connects to the peripheral and writes a message to the characteristic.
    pub async fn send_message(&self, message: &[u8]) -> anyhow::Result<()> {
        let manager = Manager::new().await?;
        let adapter = manager
            .adapters()
            .await?
            .into_iter()
            .next()
            .ok_or_else(|| anyhow::anyhow!("No Bluetooth adapters found"))?;
        adapter.start_scan(ScanFilter::default()).await?;
        sleep(Duration::from_secs(2)).await;
        for peripheral in adapter.peripherals().await? {
            if let Some(props) = peripheral.properties().await? {
                if props.services.iter().any(|s| *s == self.service_uuid) {
                    peripheral.connect().await?;
                    peripheral.discover_services().await?;
                    if let Some(ch) = peripheral
                        .characteristics()
                        .into_iter()
                        .find(|c| c.uuid == self.char_uuid)
                    {
                        peripheral.write(&ch, message, WriteType::WithResponse).await?;
                    }
                    peripheral.disconnect().await?;
                    return Ok(());
                }
            }
        }
        Err(anyhow::anyhow!("Matching BLE device not found"))
    }
}
