import { useEffect, useRef, useState } from 'react'

function KpiCard({ title, value, critical = false, status = 'ok', info }) {
    // =========================
    // Información opcional KPI
    // =========================
    //
    // La prop info es opcional.
    // Permite documentar KPIs sin
    // cambiar el comportamiento de
    // las tarjetas que no la usan.
    // Esto mejora la comprensión del
    // dashboard sin tocar cálculos.
    //
    const [isInfoOpen, setIsInfoOpen] = useState(false)
    const cardRef = useRef(null)
    const hasInfo = Boolean(info)
    const tone = critical ? 'danger' : status
    const classNames = ['kpi-card']

    // =========================
    // Cierre por click externo
    // =========================
    //
    // useRef permite saber si el
    // click ocurre fuera de la
    // tarjeta y del popover.
    // El listener se elimina para
    // evitar fugas de memoria.
    //
    useEffect(() => {
        if (!isInfoOpen) {
            return undefined
        }

        const handleOutsideClick = (event) => {
            if (
                cardRef.current
                && !cardRef.current.contains(event.target)
            ) {
                setIsInfoOpen(false)
            }
        }

        document.addEventListener('mousedown', handleOutsideClick)

        return () => {
            document.removeEventListener('mousedown', handleOutsideClick)
        }
    }, [isInfoOpen])

    if (hasInfo) {
        classNames.push('kpi-card-with-info')
    }

    if (tone === 'danger') {
        classNames.push('kpi-card-critical')
    }

    if (tone === 'warning') {
        classNames.push('kpi-card-warning')
    }

    return (

        <article ref={cardRef} className={classNames.join(' ')}>

            <h2>{title}</h2>

            <p>{value}</p>

            {hasInfo && (
                <>
                    <button
                        type="button"
                        className="kpi-info-button"
                        aria-label={`Información sobre ${title}`}
                        aria-expanded={isInfoOpen}
                        onClick={() => setIsInfoOpen(!isInfoOpen)}
                    >
                        i
                    </button>

                    {isInfoOpen && (
                        <div className="kpi-info-popover">
                            <h3 className="kpi-info-title">{title}</h3>

                            <div className="kpi-info-section">
                                <strong>Explicación</strong>
                                <p>{info.description}</p>
                            </div>

                            <div className="kpi-info-section">
                                <strong>Algoritmo</strong>
                                <p>{info.algorithm}</p>
                            </div>

                            <div className="kpi-info-section">
                                <strong>Interpretación</strong>
                                <p>{info.interpretation}</p>
                            </div>

                        </div>
                    )}
                </>
            )}

        </article>
    )
}

export default KpiCard
