use tokio::net::TcpStream;
use tokio::io::{AsyncReadExt, AsyncWriteExt};

/// Client used for sending clipboard updates to a remote device.
pub struct SyncClient {
    remote_addr: String,
}

impl SyncClient {
    pub fn new<A: Into<String>>(addr: A) -> Self {
        Self { remote_addr: addr.into() }
    }

    /// Sends a clipboard message to the remote device.
    pub async fn send_message(&self, message: &str) -> tokio::io::Result<()> {
        let mut stream = TcpStream::connect(&self.remote_addr).await?;
        stream.write_all(message.as_bytes()).await?;
        Ok(())
    }
}

/// Server that accepts incoming clipboard messages.
pub struct SyncServer {
    bind_addr: String,
}

impl SyncServer {
    pub fn new<A: Into<String>>(addr: A) -> Self {
        Self { bind_addr: addr.into() }
    }

    /// Waits for a single clipboard message from a remote client.
    pub async fn receive_message(&self) -> tokio::io::Result<String> {
        let listener = tokio::net::TcpListener::bind(&self.bind_addr).await?;
        let (mut socket, _) = listener.accept().await?;
        let mut buf = String::new();
        socket.read_to_string(&mut buf).await?;
        Ok(buf)
    }
}
