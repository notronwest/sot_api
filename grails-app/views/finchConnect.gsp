<html>
<head>
    <script src="https://prod-cdn.tryfinch.com/v1/connect.js"></script>
</head>
<body>
<button id="connect-button">Open Finch Connect</button>
<script>
    const button = document.getElementById('connect-button');
    const onSuccess = ({code}) => {
        // exchange code for access token via your server
    }
    const onError = ({ errorMessage }) => {
        console.error(errorMessage);
    }
    const onClose = () => {
        console.log('Connect closed');
    }
    const connect = FinchConnect.initialize({
        clientId: '170faba7-c1cd-4692-a9d6-29e32bb6c7f6',
        products: ['company', 'directory', 'employment'],
        sandbox: false,
        manual: false,
        onSuccess,
        onError,
        onClose,
    });
    button.addEventListener('click', () => {
        connect.open();
    })
</script>
</body>
</html>
