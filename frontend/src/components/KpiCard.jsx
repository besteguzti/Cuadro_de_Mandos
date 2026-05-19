function KpiCard({ title, value, critical = false }) {

    return (

        <article className={`kpi-card${critical ? ' kpi-card-critical' : ''}`}>

            <h2>{title}</h2>

            <p>{value}</p>

        </article>
    )
}

export default KpiCard
