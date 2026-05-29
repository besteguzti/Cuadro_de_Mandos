function PlatformSelector({ platforms, selectedPlatform, onChange }) {
  return (
    <label>
      Plataforma tecnica
      <select value={selectedPlatform} onChange={onChange}>
        {platforms.map((item) => (
          <option key={item.value} value={item.value}>
            {item.label}
          </option>
        ))}
      </select>
    </label>
  );
}

export default PlatformSelector;
