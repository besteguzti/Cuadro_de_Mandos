function KpiCard({ title, value }) {

    return (

        <div style={{

            border: '1px solid #ccc',
            borderRadius: '10px',
            padding: '20px',
            width: '200px',
            textAlign: 'center',
            margin: '10px',
            boxShadow: '0px 2px 5px rgba(0,0,0,0.2)'
        }}>

            <h3>{title}</h3>

            <h1>{value}</h1>

        </div>
    )
}

export default KpiCard